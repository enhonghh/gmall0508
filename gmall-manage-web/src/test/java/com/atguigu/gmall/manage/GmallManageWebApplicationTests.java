package com.atguigu.gmall.manage;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.atguigu.gmall.path.Path;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManageWebApplicationTests {



	@Test
	public void contextLoads() throws IOException, MyException {

		//先添加加载fdfs的配置文件,(getResource("tracker.conf").getPath();获取路径)
		String path = GmallManageWebApplicationTests.class.getClassLoader().getResource("tracker.conf").getPath();
		//所有的初始化参数，都可以通过path相关的文件读取到fdfs的的默认参数
		ClientGlobal.init(path);

		//创建一个tracker 的链接 用来与fdfs创建一个链接线程（和数据库链接类似一样）
		TrackerClient trackerClient = new TrackerClient();
		//与fdfs创建一个链接线程
		TrackerServer connection = trackerClient.getConnection();

		//从tracker中返回一个可用的storage（存储）storageServer:numm是存储的位置（存储在本机就用null）
		StorageClient storageClient = new StorageClient(connection, null);

		//上次文件(local_filename有两种写法1.上传数据的byte数组，需要上传文件的路径)
		//storageClient.upload_appender_file("","jpg",null);
		String[] jpgs = storageClient.upload_appender_file("e:/java-ziliao/图片资源/a.jpg", "jpg", null);

		 //返回上传结果（也就是路径信息，具体放在那里）本机地址你加图片地址拼串
		String imgUrl  = Path.Local_PATH;
		for (String jpg : jpgs) {

			imgUrl = imgUrl + "/" + jpg;
		}
		System.out.println(imgUrl);

	}
}
