package cn.sparrowmini.file.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

//
//@SpringBootTest
//@ActiveProfiles("test")
//public class Test {
//	@Autowired
//	private FileService fileService;
//
//	@org.junit.jupiter.api.Test
//	public void test() {
//		MockMultipartFile firstFile = new MockMultipartFile("data", "filename.txt", "text/plain",
//				"some xml".getBytes());
//
//		String id = this.fileService.upload(firstFile);
//		System.out.println(id);
//		this.fileService.dowload(id);
//	}
//}
