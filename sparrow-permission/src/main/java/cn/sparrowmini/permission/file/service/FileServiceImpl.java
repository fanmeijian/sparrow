package cn.sparrowmini.permission.file.service;

import cn.sparrowmini.common.model.ApiResponse;
import cn.sparrowmini.common.model.BaseFile;
import cn.sparrowmini.common.service.StorageService;
import cn.sparrowmini.permission.file.model.SparrowFile;
import cn.sparrowmini.permission.file.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RestController
public class FileServiceImpl implements FileService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private StorageService storageService;

    @Override
    public BaseFile upload(MultipartFile file) {
        try {
            return this.storageService.upload(file.getInputStream(),file.getOriginalFilename());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] download(String id) {
        return this.storageService.download(this.fileRepository.findById(id).orElseThrow());
    }

    @Override
    public Page<SparrowFile> listFile(Pageable pageable, String filters) {
        return fileRepository.findAll(pageable, filters);
    }

    @Transactional
    @Override
    public ApiResponse<List<String>> saveFile(List<SparrowFile> files) {
        fileRepository.saveAll(files);
        return new ApiResponse<>(files.stream().map(BaseFile::getId).toList());
    }

    @Override
    public SparrowFile getFile(String fileId) {
        return fileRepository.findById(fileId).orElseThrow();
    }
}
