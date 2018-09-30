package com.atguigu.gmall.manage.util;

import com.atguigu.gmall.path.Path;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class FileUploadUtil {

    public static String uploadImage(MultipartFile file) {

        //先添加加载fdfs的配置文件,(getResource("tracker.conf").getPath();获取路径)
        String path = FileUploadUtil.class.getClassLoader().getResource("tracker.conf").getPath();

        //所有的初始化参数，都可以通过path相关的文件读取到fdfs的的默认参数
        try {
            ClientGlobal.init(path);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        //创建一个tracker 的链接 用来与fdfs创建一个链接线程（和数据库链接类似一样）
        TrackerClient trackerClient = new TrackerClient();
        //与fdfs创建一个链接线程
        TrackerServer connection = null;
        try {
            connection = trackerClient.getConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //从tracker中返回一个可用的storage（存储）storageServer:numm是存储的位置（存储在本机就用null）
        StorageClient storageClient = new StorageClient(connection, null);

        //上传文件(local_filename有两种写法1.上传数据的byte数组，2需要上传文件的路径)
        //storageClient.upload_appender_file("","jpg",null);
        String[] jpgs = new String[0];
        try {
            //获取文件名
            String originalFilename = file.getOriginalFilename();
            //按名字的.截取（获取最后一个节点）
            String[] split = originalFilename.split("\\.");
            //文件的长度减一获取最后的.的节点
            String extName = split[(split.length - 1)];


            jpgs = storageClient.upload_appender_file(file.getBytes(), extName, null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        //返回上传结果（也就是路径信息，具体放在那里）本机地址你加图片地址拼串
        String imgUrl  = Path.Local_PATH;

        for (String jpg : jpgs) {

            imgUrl = imgUrl + "/" + jpg;
        }
        System.out.println(imgUrl);

    return  imgUrl;
    }
}
