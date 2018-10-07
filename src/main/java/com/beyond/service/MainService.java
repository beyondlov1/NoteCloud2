package com.beyond.service;

import com.beyond.FxDocument;
import com.beyond.MainController;
import com.beyond.RepositoryFactory;
import com.beyond.entity.Document;
import com.beyond.f.F;
import com.beyond.repository.LocalDocumentRepository;
import com.beyond.repository.RemoteDocumentRepository;
import com.beyond.repository.Repository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Main
 */
public class MainService {
    private Repository<Document> defaultLocalRepository;
    private Repository<Document> defaultRemoteRepository;
    private Repository<Document> deletedLocalRepository;

    private ObservableList<FxDocument> fxDocuments;
    private MainController mainController;

    public MainService(){
        init();
    }
    public MainService(MainController mainController){
        this.init();
        this.mainController = mainController;
    }

    /**
     * 初始化
     */
    private void  init(){
        this.defaultLocalRepository = RepositoryFactory.getLocalRepository(F.DEFAULT_LOCAL_PATH);
        this.defaultRemoteRepository = new LocalDocumentRepository(F.DEFAULT_TMP_PATH);
        this.deletedLocalRepository = RepositoryFactory.getLocalRepository(F.DEFAULT_DELETE_PATH);
        setFxDocuments();
    }

    public void setFxDocuments() {
        List<FxDocument> fxDocuments = new ArrayList<>();
        List<Document> documents = findAll();
        for (Document document : documents) {
            FxDocument fxDocument = new FxDocument(document);
            fxDocuments.add(fxDocument);
        }
        this.fxDocuments =  FXCollections.observableList(fxDocuments);
    }

    /**
     * 添加文档
     * @param document
     * @return
     */
    public String add(Document document){
        Serializable id = defaultLocalRepository.add(document);
        defaultLocalRepository.save();

        //同步fxDocument
        FxDocument fxDocument = new FxDocument(document);
        fxDocuments.add(0,fxDocument);
        mainController.getDocumentTableView().refresh();
        return (String) id;
    }

    /**
     * 删除文档
     * @param id
     * @return
     */
    public String deleteById(String id){
        Document document = new Document();
        document.setId(id);

        Document foundDocument = defaultLocalRepository.select(id);

        if (foundDocument==null){
            return null;
        }

        Serializable foundId = defaultLocalRepository.delete(foundDocument);
        defaultLocalRepository.save();

        deletedLocalRepository.add(foundDocument);
        deletedLocalRepository.save();

        //同步fxDocument
        int index = -1;
        for (int i = 0; i < fxDocuments.size(); i++) {
            if (StringUtils.equals(fxDocuments.get(i).getId(),id)){
                index = i;
                break;
            }
        }
        if (index!=-1){
            fxDocuments.remove(index);
        }
        mainController.getDocumentTableView().refresh();
        return (String) foundId;
    }

    /**
     * 更新文档
     * @param document
     * @return
     */
    public String update(Document document){
        Serializable id = defaultLocalRepository.update(document);
        defaultLocalRepository.save();

        if (!"JavaFX Application Thread".equals(Thread.currentThread().getName())) return (String)id;//防止其他线程刷新

        //同步fxDocument
        int index = -1;
        for (int i = 0; i < fxDocuments.size(); i++) {
            if (StringUtils.equals(fxDocuments.get(i).getId(),document.getId())){
                index = i;
                break;
            }
        }
        if (index!=-1){
            fxDocuments.set(index,new FxDocument(document));
        }
        return (String) id;
    }

    /**
     * 查询本地所有文档
     * @return
     */
    public List<Document> findAll(){
        return defaultLocalRepository.selectAll();
    }

    /**
     * 查询本地单个文档
     * @param id
     * @return
     */
    public Document find(String id){
        return defaultLocalRepository.select(id);
    }

    /**
     * 获取所有FxDocument
     * @return
     */
    public ObservableList<FxDocument> getFxDocuments() {
        return fxDocuments;
    }

    public void pull(){
        defaultLocalRepository.pull();
    }
}
