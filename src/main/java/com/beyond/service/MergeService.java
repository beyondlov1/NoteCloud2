package com.beyond.service;

import com.beyond.entity.Document;
import com.beyond.f.F;
import com.beyond.property.FileRemotePropertyManager;
import com.beyond.property.LocalPropertyManager;
import com.beyond.property.PropertyManager;
import com.beyond.repository.impl.LocalDocumentRepository;
import com.beyond.repository.impl.RemoteDocumentRepository;
import com.beyond.repository.Repository;
import com.beyond.utils.ListUtils;
import javafx.application.Platform;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 同步服务
 */
public class MergeService extends Observable {
    private Repository<Document> localRepository;
    private Repository<Document> remoteRepository;
    private PropertyManager localPropertyManager;
    private PropertyManager remotePropertyManager;
    private LocalPropertyManager remoteLocalPropertyManager;
    private LocalDocumentRepository remoteLocalDocumentRepository;
    private int failCount = 0;

    private int mergeFlag = 0;

    public MergeService(String path, String url, String tmpPath) {
        super();
        this.localRepository = new LocalDocumentRepository(path);
        this.localPropertyManager = new LocalPropertyManager(path);
        this.remoteLocalPropertyManager = new LocalPropertyManager(tmpPath);
        this.remoteLocalDocumentRepository = new LocalDocumentRepository(tmpPath);
//        this.remotePropertyManager = new RemotePropertyManager(url);
        this.remotePropertyManager = new FileRemotePropertyManager(F.DEFAULT_REMOTE_FILE_INFO_PATH);
        this.remoteRepository = new RemoteDocumentRepository(url, remoteLocalDocumentRepository, remoteLocalPropertyManager,remotePropertyManager);
    }

    /**
     * 同步
     */
    public synchronized void handle() {
        Map<String, String> localPropertiesMap = localPropertyManager.getAllProperties();
        Map<String, String> remotePropertiesMap = remotePropertyManager.getAllProperties();
        String localLastModifyTime = localPropertiesMap.getOrDefault("_lastModifyTime", "0");
        String remoteLastModifyTime = remotePropertiesMap.getOrDefault("_lastModifyTime", "0");
        if (!StringUtils.equals(localLastModifyTime, "")
                && !StringUtils.equals(localLastModifyTime, "0")
                && StringUtils.equals(localLastModifyTime, remoteLastModifyTime)) {
            return;
        }

        if (!remoteRepository.isAvailable()) {
            failCount++;
            if (failCount > 10) {//连接10次失败, 强制解锁
                remoteRepository.unlock();
                failCount = 0;
            }
            return;
        }

        //锁
        remoteRepository.lock();

        List<Document> merge = merge();

        //设置属性
        String localVersion = localPropertiesMap.getOrDefault("_version", "0");
        String remoteVersion = remotePropertiesMap.getOrDefault("_version", "0");
        String currentTime = System.currentTimeMillis() + "";
        if (StringUtils.isNotBlank(localPropertiesMap.getOrDefault("_modifyIds", ""))) {
            localPropertyManager.set("_lastModifyTime", currentTime);
            remoteLocalPropertyManager.set("_lastModifyTime", currentTime);
        } else {
            String remoteTime = remotePropertiesMap.getOrDefault("_lastModifyTime", "");
            if (StringUtils.isBlank(remoteTime)){
                remoteTime = currentTime;
            }
            localPropertyManager.set("_lastModifyTime", remoteTime);
            remoteLocalPropertyManager.set("_lastModifyTime", remoteTime);
        }
        localPropertyManager.set("_version", localVersion.compareTo(remoteVersion) < 0 ? remoteVersion : localVersion);
        remoteLocalPropertyManager.set("_version", localVersion.compareTo(remoteVersion) < 0 ? remoteVersion : localVersion);
        //清空modifyIds
        localPropertyManager.set("_modifyIds", "");
        remoteLocalPropertyManager.set("_modifyIds", "");

        //持久化
        int isRemoteSaveSuccess = remoteRepository.save(merge);
        int isLocalSaveSuccess = localRepository.save(merge);

        //解锁
        remoteRepository.unlock();

        if (isLocalSaveSuccess==1&&isRemoteSaveSuccess==1){
            mergeFlag = 1;
            setChanged();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    notifyObservers();
                }
            });
        }
    }

    /**
     * 合并本地与远程
     *
     * @return 合并后列表
     */
    private List<Document> merge() {
        //获取本地和远程的文档
        localRepository.pull();
        List<Document> localList = localRepository.selectAll();
        remoteRepository.pull();
        List<Document> remoteList = remoteRepository.selectAll();

        Collections.reverse(localList);
        Collections.reverse(remoteList);

        //获取远程和本地所有id
        List<String> remoteDocumentIds = new ArrayList<>();
        List<String> localDocumentIds = new ArrayList<>();
        for (Document remoteDocument : remoteList) {
            remoteDocumentIds.add(remoteDocument.getId());
        }
        for (Document localDocumentId : localList) {
            localDocumentIds.add(localDocumentId.getId());
        }

        //获取动过的id
        List<String> deletedDocumentIds = new ArrayList<>();
        List<String> modifyDocumentIds = new ArrayList<>();
        List<String> addDocumentIds = new ArrayList<>();
        String modifyIdsStr = localPropertyManager.getProperty("_modifyIds");
        if (StringUtils.isBlank(modifyIdsStr)) return remoteList;//如果没有修改过, 直接返回远程列表
        String[] modifyIds = modifyIdsStr.substring(0, modifyIdsStr.length() - 1).split(",");//获取更改过的id
        Set<String> modifyIdsSet = new HashSet<>(Arrays.asList(modifyIds));//去重

        for (String modifyId : modifyIdsSet) {
            if (localDocumentIds.contains(modifyId)) {
                if (remoteDocumentIds.contains(modifyId)) {
                    modifyDocumentIds.add(modifyId);
                } else {
                    addDocumentIds.add(modifyId);
                }
            } else {
                deletedDocumentIds.add(modifyId);
            }
        }


        //以远程的为主
        List<Document> result = new ArrayList<>();
        for (Document remoteDocument : remoteList) {
            if (deletedDocumentIds.contains(remoteDocument.getId())) {//删除的不添加
                continue;
            }
            if (modifyDocumentIds.contains(remoteDocument.getId())) {//本地更新的
                Document localDocument = ListUtils.getDocumentById(localList, remoteDocument.getId());
                if (localDocument != null && localDocument.getLastModifyTime().compareTo(remoteDocument.getLastModifyTime()) < 0) {
                    if (localDocument.getVersion() < remoteDocument.getVersion()) {
                        result.add(remoteDocument);
                    } else {
                        result.add(localDocument);
                    }
                } else {
                    result.add(localDocument);
                }
                continue;
            }
            result.add(remoteDocument);
        }

        for (String addDocumentId : addDocumentIds) {//本地添加的
            result.add(ListUtils.getDocumentById(localList, addDocumentId));
        }

        return result;
    }

    public int getMergeFlag() {
        return mergeFlag;
    }

    public void setMergeFlag(int mergeFlag) {
        this.mergeFlag = mergeFlag;
    }

    public static void main(String[] args) {
        MergeService mergeService = new MergeService("./repository/documents.xml",
                "https://yura.teracloud.jp/dav/NoteCloud/repository/documents.xml",
                "./repository/tmp.xml");
        mergeService.handle();
    }
}
