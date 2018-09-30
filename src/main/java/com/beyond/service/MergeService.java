package com.beyond.service;

import com.beyond.entity.Document;
import com.beyond.f.F;
import com.beyond.property.LocalPropertyManager;
import com.beyond.property.PropertyManager;
import com.beyond.property.RemotePropertyManager;
import com.beyond.repository.LocalDocumentRepository;
import com.beyond.repository.RemoteDocumentRepository;
import com.beyond.repository.Repository;
import com.beyond.utils.ListUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 同步服务
 */
public class MergeService {
    private Repository<Document> localRepository;
    private Repository<Document> remoteRepository;
    private PropertyManager localPropertyManager;
    private PropertyManager remotePropertyManager;
    private LocalPropertyManager remoteLocalPropertyManager;
    private LocalDocumentRepository remoteLocalDocumentRepository;

//    public MergeService(LocalDocumentRepository localRepository, RemoteDocumentRepository remoteRepository, PropertyManager localPropertyManager, PropertyManager remotePropertyManager) {
//        this.localRepository = localRepository;
//        this.remoteRepository = remoteRepository;
//        this.localPropertyManager = localPropertyManager;
//        this.remotePropertyManager = remotePropertyManager;
//    }

    public MergeService(String path, String url, String tmpPath) {
        this.localRepository = new LocalDocumentRepository(path);
        this.localPropertyManager = new LocalPropertyManager(path);
        this.remoteLocalPropertyManager = new LocalPropertyManager(tmpPath);
        this.remoteLocalDocumentRepository = new LocalDocumentRepository(tmpPath);
        this.remoteRepository = new RemoteDocumentRepository(url, remoteLocalDocumentRepository, remoteLocalPropertyManager);
        this.remotePropertyManager = new RemotePropertyManager(url);
    }

    @Deprecated
    public void asynHandle(){
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                handle();
            }
        });
    }

    /**
     * 同步
     */
    public synchronized void handle(){
        Map<String ,String> localProrpertiesMap = localPropertyManager.getAllProperties();
        Map<String, String> remotePropertiesMap = remotePropertyManager.getAllProperties();
        String localLastModifyTime = localProrpertiesMap.getOrDefault("_lastModifyTime","0");
        String remoteLastModifyTime = remotePropertiesMap.getOrDefault("_lastModifyTime","0");
        if (!StringUtils.equals(localLastModifyTime,"")
                && !StringUtils.equals(localLastModifyTime,"0")
                &&StringUtils.equals(localLastModifyTime, remoteLastModifyTime)) {
            return;
        }

        while (!remoteRepository.isAvailable()){
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                F.logger.info(e.getMessage());
            }
        }

        remoteRepository.lock();

        List<Document> merge = merge();

        //设置属性
        String localVersion =localProrpertiesMap.getOrDefault("_version","0");
        String remoteVersion =  remotePropertiesMap.getOrDefault("_version","0");
        localPropertyManager.set("_lastModifyTime",localLastModifyTime.compareTo(remoteLastModifyTime)<0?remoteLastModifyTime:localLastModifyTime);
        remoteLocalPropertyManager.set("_lastModifyTime",localLastModifyTime.compareTo(remoteLastModifyTime)<0?remoteLastModifyTime:localLastModifyTime);
        localPropertyManager.set("_version",localVersion.compareTo(remoteVersion)<0?remoteVersion:localVersion);
        remoteLocalPropertyManager.set("_version",localVersion.compareTo(remoteVersion)<0?remoteVersion:localVersion);
        //清空modifyIds
        localPropertyManager.set("_modifyIds","");
        remoteLocalPropertyManager.set("_modifyIds","");

        localRepository.save(merge);
        remoteRepository.save(merge);

        remoteRepository.unlock();
    }

    /**
     * 合并本地与远程
     * @return 合并后列表
     */
    private List merge() {
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
        if (StringUtils.isBlank(modifyIdsStr)) return remoteList;//如果没有修改过, 直接返回
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

    public static void main(String[] args) {
        MergeService mergeService = new MergeService("./document/documents.xml",
                "https://yura.teracloud.jp/dav/NoteCloud/repository/documents.xml",
                "./document/tmp.xml");
        mergeService.handle();
    }
}
