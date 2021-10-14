package cn.sparrow.common.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.sparrow.common.repository.UrlRepository;
import cn.sparrow.model.url.SparrowUrl;

@Service
public class UrlService {
  @Autowired UrlRepository urlRepository;

  public int saveUrls(List<SparrowUrl> sparrowUrls) {
    return urlRepository.saveAll(sparrowUrls).size();
  }
  
  public SparrowUrl updateUrl(SparrowUrl sparrowUrl) {
    return urlRepository.save(sparrowUrl);
  }
  
  public SparrowUrl getUrl(String id) {
    return urlRepository.findById(id).get();
  }
  
  public void delUrls(List<String> sparrowUrls) {
    sparrowUrls.forEach(f->{
      urlRepository.deleteById(f);
    });
  }
}