package cn.sparrowmini.form.service;


import cn.sparrowmini.form.model.SparrowForm;
import cn.sparrowmini.form.model.SparrowFormData;
import cn.sparrowmini.form.repository.FormDataRepository;
import cn.sparrowmini.form.repository.FormRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class SparrowFormServiceImpl implements SparrowFormService {
	@Autowired
	private FormRepository formRepository;
	@Autowired
	private FormDataRepository formDataRepository;

	@Override
	public void saveForm(SparrowForm form) {
		this.formRepository.save(form);
	}

	@Override
	public SparrowForm getForm(String formId) {
		return this.formRepository.findById(formId).get();
	}

	@Override
	public void saveData(String formId, String data) {
		this.formDataRepository.save(new SparrowFormData(formId, data));

	}

	@Override
	public SparrowFormData getFormData(String formDataId) {
		return this.formDataRepository.findById(formDataId).get();
	}

	@Override
	public Page<SparrowForm> getForms(Pageable pageable) {
		return this.formRepository.findAll(pageable);
	}

	@Override
	public Page<SparrowFormData> getFormDatas(String formId, Pageable pageable) {
		if(this.formRepository.existsById(formId)) {
			return this.formDataRepository.findByFormId(formId, pageable);
		}else{
			SparrowForm formRef = this.formRepository.getReferenceByCode(formId);
			if(formRef != null) {
				return this.formDataRepository.findByFormId(formRef.getId(), pageable);
			}
			return null;
		}
	}

	@Override
	public Page<SparrowFormData> getFormsDatas(Pageable pageable) {
		return this.formDataRepository.findAll(pageable);
	}

	@Override
	public void updateForm(String formId, SparrowForm form) {
		SparrowForm updateForm = this.formRepository.getReferenceById(formId);
		updateForm.setName(form.getName());
		updateForm.setCode(form.getCode());
		updateForm.setForm(form.getForm());
		updateForm.setDisplayColumns(form.getDisplayColumns());
		this.formRepository.save(updateForm);
	}

	@Override
	public void deleteForm(Set<String> ids) {
		this.formRepository.deleteAllById(ids);
		
	}

	@Override
	public void updateData(String dataId, String data) {
		SparrowFormData formData = this.formDataRepository.getReferenceById(dataId);
		formData.setData(data);
		this.formDataRepository.save(formData);
	}

	@Override
	public void deleteFormData(Set<String> ids) {
		this.formDataRepository.deleteAllById(ids);
		
	}

}
