package cn.sparrowmini.owl.solr.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.CaseFormat;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.util.StringUtils;
// 引入 SolrJ 注解替换 Spring Data Solr
import org.apache.solr.client.solrj.beans.Field;

public class ItemType extends BaseMetadataObject implements ICatalogueItem, Serializable {
    public static String QUALIFIED_DELIMITER = "@";
    private static final long serialVersionUID = -3631731059281154372L;

    @Field("doctype")
    private String type = "item";

    @Field("catalogueId")
    private String catalogueId;

    @Field("*_currency")
    private Map<String, String> currencyMap = new HashMap<>();

    @Field("*_price")
    private Map<String, Double> currencyValue = new HashMap<>();

    @Field("applicableCountries")
    private Set<String> applicableCountries;

    @Field("freeOfCharge")
    private Boolean freeOfCharge;

    @Field("priceHidden")
    private Boolean priceHidden;

    @Field("customizable")
    private Boolean customizable;

    @Field("sparePart")
    private Boolean sparePart;

    @Field("certificateType")
    private Set<String> certificateType;

    @Field("circularEconomyCertificates")
    private Set<String> circularEconomyCertificates;

    @Field("permittedParties")
    private Set<String> permittedParties;

    @Field("restrictedParties")
    private Set<String> restrictedParties;

    @Field("*_deliveryTimeUnit")
    private Map<String, String> deliveryTimeUnit = new HashMap<>();

    @Field("*_deliveryTime")
    private Map<String, Double> deliveryTime = new HashMap<>();

    @Field("*_packageUnit")
    private Map<String, String> packageUnit = new HashMap<>();

    @Field("*_package")
    private Map<String, List<Double>> packageAmounts = new HashMap<>();

    @Field("manufacturerId")
    private String manufacturerId;

    @Field("manufacturerItemId")
    private String manufactuerItemId;

    @Field("serviceType")
    private Set<String> serviceType;

    @Field("supportedProductNature")
    private String supportedProductNature;

    @Field("supportedCargoType")
    private String supportedCargoType;

    @Field("emissionStandard")
    private String emissionStandard;

    @Field("creationDate")
    private String creationDate;

    @Field("*_baseQuantityUnit")
    private Map<String, String> baseQuantityUnits = new HashMap<>();

    @Field("*_baseQuantity")
    private Map<String, List<Double>> baseQuantities = new HashMap<>();

    @Field("commodityClassficationUri")
    private List<String> classificationUri;

    @Field("*_key")
    private Map<String, String> propertyMap = new HashMap<>();

    @Field("*_svalues")
    private Map<String, Collection<String>> stringValue = new HashMap<>();

    @Field("*_bvalue")
    private Map<String, Boolean> booleanValue = new HashMap<>();

    @Field("*_dvalues")
    private Map<String, Collection<Double>> doubleValue = new HashMap<>();

    @Field("image")
    private Collection<String> imgageUri;

    @Field("incoterm")
    private Collection<String> incoterms;

    @Field("minimumOrderQuantity")
    private Double minimumOrderQuantity;

    @Field("warrantyValidityPeriod")
    private Double warrantyValidityPeriod;

    @ReadOnlyProperty
    private List<BaseMetadataObject> classification;

    @ReadOnlyProperty
    private PartyType manufacturer;

    @ReadOnlyProperty
    private Map<String, PropertyType> customProperties;

    public ItemType() {
    }

    public void setStringProperty(String qualifier, Collection<String> values) {
        this.stringValue.put(this.dynamicKey(qualifier, this.propertyMap), values);
    }

    @SuppressWarnings("unchecked")
    public void addProperty(String qualifier, String value) {
        String key = this.dynamicKey(qualifier, this.propertyMap);
        Collection<String> values = this.stringValue.get(key);
        if (values == null) {
            values = new HashSet<>();
            this.stringValue.put(key, values);
        }

        values.add(value);
    }

    public void addProperty(String qualifier, String value, PropertyType meta) {
        this.addProperty(qualifier, value);
        if (meta != null) {
            this.addCustomProperty(qualifier, meta, ValueQualifier.STRING);
        }
    }

    @SuppressWarnings("unchecked")
    public void addProperty(String qualifier, String unit, Double value) {
        String key = this.dynamicKey(this.propertyMap, qualifier, unit);
        Collection<Double> values = this.doubleValue.get(key);
        if (values == null) {
            values = new HashSet<>();
            this.doubleValue.put(key, values);
        }

        values.add(value);
    }

    public void addProperty(String qualifier, String unit, Double value, PropertyType meta) {
        this.addProperty(qualifier, unit, value);
        if (meta != null) {
            this.addCustomProperty(qualifier, unit, meta, ValueQualifier.QUANTITY);
        }
    }

    /** @deprecated */
    @Deprecated
    public void addProperty(String qualifier, String unit, String value, PropertyType meta) {
        this.addMultiLingualProperty(qualifier, unit, value, meta);
    }

    private void setDoubleProperty(String qualifier, Collection<Double> values) {
        if (qualifier.contains("@")) {
            String qualifiedValue = qualifiedValue(qualifier);
            String qualifierUnit = qualifiedUnit(qualifier);

            for(Double d : values) {
                this.addProperty(qualifiedValue, qualifierUnit, d);
            }
        } else {
            for(Double d : values) {
                this.addProperty(qualifier, d);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void addProperty(String qualifier, Double value) {
        String key = this.dynamicKey(qualifier, this.propertyMap);
        Collection<Double> values = this.doubleValue.get(key);
        if (values == null) {
            values = new HashSet<>();
            this.doubleValue.put(key, values);
        }

        values.add(value);
    }

    public void addProperty(String qualifier, Double value, PropertyType meta) {
        this.addProperty(qualifier, value);
        if (meta != null) {
            this.addCustomProperty(qualifier, meta, ValueQualifier.NUMBER);
        }
    }

    private void addCustomProperty(String qualifier, String unit, PropertyType meta, ValueQualifier valueQualfier) {
        String key = dynamicFieldPart(qualifier);
        String full = dynamicFieldPart(qualifier, unit);
        if (this.customProperties == null) {
            this.customProperties = new HashMap<>();
        }

        if (!this.customProperties.containsKey(key)) {
            this.customProperties.put(key, meta);
        }

        PropertyType pt = this.customProperties.get(key);
        pt.setValueQualifier(valueQualfier);
        pt.addItemFieldName(key);
        pt.addItemFieldName(full);
    }

    private void addCustomProperty(String qualifier, PropertyType meta, ValueQualifier valueQualifier) {
        String part = dynamicFieldPart(qualifier);
        if (this.customProperties == null) {
            this.customProperties = new HashMap<>();
        }

        if (!this.customProperties.containsKey(part)) {
            this.customProperties.put(part, meta);
        }

        PropertyType pt = this.customProperties.get(part);
        pt.setValueQualifier(valueQualifier);
        pt.addItemFieldName(part);
    }

    /** @deprecated */
    @Deprecated
    public void addProperty(String qualifier, String unit, String value) {
        this.addMultiLingualProperty(qualifier, unit, value);
    }

    @SuppressWarnings("unchecked")
    public void addMultiLingualProperty(String qualifier, String language, String text) {
        String key = this.dynamicKey(this.propertyMap, qualifier);
        Collection<String> values = this.stringValue.get(key);
        if (values == null) {
            values = new HashSet<>();
            this.stringValue.put(key, values);
        }

        values.add(String.format("%s@%s", text, language));
    }

    public void addMultiLingualProperty(String qualifier, String language, String text, PropertyType meta) {
        this.addMultiLingualProperty(qualifier, language, text);
        if (meta != null) {
            this.addCustomProperty(qualifier, meta, ValueQualifier.STRING);
        }
    }

    // 这里的 lambda 表达式由于反编译缺失具体类名（new 2(this) 等），
    // 实际在使用时通常应该转换为标准的 Java Lambda：
    public String getMultiLingualProperty(String qualifier, String language) {
        String key = dynamicFieldPart(qualifier);
        if (this.stringValue.get(key) != null && !this.stringValue.get(key).isEmpty()) {
            Optional<String> prop = this.stringValue.get(key).stream()
                    .filter(str -> str.endsWith("@" + language))
                    .map(str -> str.split("@")[0])
                    .findFirst();
            return prop.orElse(null);
        } else {
            return null;
        }
    }

    public List<String> getMultiLingualProperties(String qualifier, String language) {
        String key = dynamicFieldPart(qualifier);
        if (this.stringValue.get(key) != null && !this.stringValue.get(key).isEmpty()) {
            return this.stringValue.get(key).stream()
                    .filter(str -> str.endsWith("@" + language))
                    .map(str -> str.split("@")[0])
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    public static String qualifiedValue(String t) {
        int delim = t.lastIndexOf("@");
        return delim > 0 ? t.substring(0, delim) : t;
    }

    public static String qualifiedUnit(String t) {
        int delim = t.lastIndexOf("@");
        return delim > 0 && t.length() > delim + 1 ? t.substring(delim + 1) : null;
    }

    public Collection<Double> getProperty(String qualifier, String unit) {
        String key = dynamicFieldPart(qualifier, unit);
        return this.doubleValue.get(key);
    }

    public Map<String, Boolean> getBooleanValue() {
        Map<String, Boolean> result = new HashMap<>();

        for(String dynUnitKey : this.propertyMap.keySet()) {
            if (this.booleanValue.containsKey(dynUnitKey)) {
                result.put(this.propertyMap.get(dynUnitKey), this.booleanValue.get(dynUnitKey));
            }
        }

        return result;
    }

    public void setBooleanValue(Map<String, Boolean> booleanValue) {
        if (booleanValue != null) {
            for(String key : booleanValue.keySet()) {
                this.booleanValue.put(this.dynamicKey(key, this.propertyMap), booleanValue.get(key));
            }
        } else {
            this.booleanValue = booleanValue;
        }
    }

    public Map<String, Collection<String>> getStringValue() {
        Map<String, Collection<String>> result = new HashMap<>();

        for(String dynUnitKey : this.propertyMap.keySet()) {
            if (this.stringValue.containsKey(dynUnitKey)) {
                result.put(this.propertyMap.get(dynUnitKey), this.stringValue.get(dynUnitKey));
            }
        }

        return result;
    }

    public Map<String, Collection<Double>> getDoubleValue() {
        Map<String, Collection<Double>> result = new HashMap<>();

        for(String dynUnitKey : this.propertyMap.keySet()) {
            if (this.doubleValue.containsKey(dynUnitKey)) {
                result.put(this.propertyMap.get(dynUnitKey), this.doubleValue.get(dynUnitKey));
            }
        }

        return result;
    }

    public void setStringValue(Map<String, Collection<String>> stringValue) {
        if (stringValue != null) {
            for(String key : stringValue.keySet()) {
                this.setStringProperty(key, stringValue.get(key));
            }
        } else {
            this.stringValue = stringValue;
        }
    }

    public void setDoubleValue(Map<String, Collection<Double>> doubleValue) {
        if (doubleValue != null) {
            for(String key : doubleValue.keySet()) {
                this.setDoubleProperty(key, doubleValue.get(key));
            }
        } else {
            this.doubleValue = doubleValue;
        }
    }

    public void setProperty(String qualifier, Boolean value) {
        this.booleanValue.put(this.dynamicKey(qualifier, this.propertyMap), value);
    }

    public void setProperty(String qualifier, Boolean value, PropertyType meta) {
        this.setProperty(qualifier, value);
        if (meta != null) {
            this.addCustomProperty(qualifier, meta, ValueQualifier.BOOLEAN);
        }
    }

    @JsonIgnore
    public String getTypeValue() {
        return this.type;
    }

    public void setTypeValue(String type) {
        this.type = type;
    }

    public String getCatalogueId() {
        return this.catalogueId;
    }

    public void setCatalogueId(String catalogueId) {
        this.catalogueId = catalogueId;
    }

    public Boolean getFreeOfCharge() {
        return this.freeOfCharge;
    }

    public void setFreeOfCharge(Boolean freeOfCharge) {
        this.freeOfCharge = freeOfCharge;
    }

    public Boolean getPriceHidden() {
        return this.priceHidden;
    }

    public void setPriceHidden(Boolean priceHidden) {
        this.priceHidden = priceHidden;
    }

    public Boolean getCustomizable() {
        return this.customizable;
    }

    public void setCustomizable(Boolean customizable) {
        this.customizable = customizable;
    }

    public Boolean getSparePart() {
        return this.sparePart;
    }

    public void setSparePart(Boolean sparePart) {
        this.sparePart = sparePart;
    }

    public Set<String> getCertificateType() {
        return this.certificateType;
    }

    public void setCertificateType(Set<String> certificateType) {
        this.certificateType = certificateType;
    }

    public Set<String> getCircularEconomyCertificates() {
        return this.circularEconomyCertificates;
    }

    public void setCircularEconomyCertificates(Set<String> circularEconomyCertificates) {
        this.circularEconomyCertificates = circularEconomyCertificates;
    }

    public Set<String> getPermittedParties() {
        return this.permittedParties;
    }

    public void setPermittedParties(Set<String> permittedParties) {
        this.permittedParties = permittedParties;
    }

    public Set<String> getRestrictedParties() {
        return this.restrictedParties;
    }

    public void setRestrictedParties(Set<String> restrictedParties) {
        this.restrictedParties = restrictedParties;
    }

    public Set<String> getApplicableCountries() {
        return this.applicableCountries;
    }

    public void setApplicableCountries(Set<String> applicableCountries) {
        this.applicableCountries = applicableCountries;
    }

    public String getCreationDate() {
        return this.creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getManufacturerId() {
        return this.manufacturer != null && this.manufacturer.getId() != null ? this.manufacturer.getId() : this.manufacturerId;
    }

    public void setManufacturerId(String manufacturerId) {
        this.manufacturerId = manufacturerId;
    }

    public Set<String> getServiceType() {
        return this.serviceType;
    }

    public void setServiceType(Set<String> serviceType) {
        this.serviceType = serviceType;
    }

    public String getSupportedProductNature() {
        return this.supportedProductNature;
    }

    public void setSupportedProductNature(String supportedProductNature) {
        this.supportedProductNature = supportedProductNature;
    }

    public String getSupportedCargoType() {
        return this.supportedCargoType;
    }

    public void setSupportedCargoType(String supportedCargoType) {
        this.supportedCargoType = supportedCargoType;
    }

    public String getEmissionStandard() {
        return this.emissionStandard;
    }

    public void setEmissionStandard(String emissionStandard) {
        this.emissionStandard = emissionStandard;
    }

    public void addPrice(String currency, Double price) {
        this.currencyValue.put(this.dynamicKey(currency, this.currencyMap), price);
    }

    public Collection<String> getCurrency() {
        return this.currencyMap.values();
    }

    public void setCurrency(Collection<String> currency) {
        this.currencyMap.clear();

        for(String c : currency) {
            this.dynamicKey(c, this.currencyMap);
        }
    }

    public Map<String, Double> getPrice() {
        Map<String, Double> ret = new HashMap<>();

        for(String key : this.currencyMap.keySet()) {
            ret.put(this.currencyMap.get(key), this.currencyValue.get(key));
        }

        return ret;
    }

    public void setPrice(Map<String, Double> price) {
        this.currencyValue.clear();

        for(String c : price.keySet()) {
            this.addPrice(c, price.get(c));
        }
    }

    @JsonIgnore
    public Map<String, String> getDeliveryTimeUnit() {
        return this.deliveryTimeUnit;
    }

    public Collection<String> getDeliveryTimeUnits() {
        return this.deliveryTimeUnit.values();
    }

    public void setDeliveryTimeUnits(Collection<String> units) {
        this.deliveryTimeUnit.clear();

        for(String unit : units) {
            this.dynamicKey(unit, this.deliveryTimeUnit);
        }
    }

    public void addDeliveryTime(String unit, Double time) {
        this.deliveryTime.put(this.dynamicKey(unit, this.deliveryTimeUnit), time);
    }

    public Map<String, Double> getDeliveryTime() {
        Map<String, Double> result = new HashMap<>();

        for(String dynUnitKey : this.deliveryTimeUnit.keySet()) {
            result.put(this.deliveryTimeUnit.get(dynUnitKey), this.deliveryTime.get(dynUnitKey));
        }

        return result;
    }

    public void setDeliveryTime(Map<String, Double> deliveryTime) {
        this.deliveryTime.clear();

        for(String c : deliveryTime.keySet()) {
            this.addDeliveryTime(c, deliveryTime.get(c));
        }
    }

    @JsonIgnore
    public Map<String, String> getPackageUnit() {
        return this.packageUnit;
    }

    public Collection<String> getPackageUnits() {
        return this.packageUnit.values();
    }

    public void setPackageUnits(Collection<String> units) {
        this.packageUnit.clear();

        for(String unit : units) {
            this.dynamicKey(unit, this.packageUnit);
        }
    }

    public void addPackageAmounts(String unit, List<Double> amounts) {
        this.packageAmounts.put(this.dynamicKey(unit, this.packageUnit), amounts);
    }

    @SuppressWarnings("unchecked")
    public Map<String, List<Double>> getPackageAmounts() {
        Map<String, List<Double>> result = new HashMap<>();

        for(String dynUnitKey : this.packageUnit.keySet()) {
            result.put(this.packageUnit.get(dynUnitKey), this.packageAmounts.get(dynUnitKey));
        }

        return result;
    }

    public void setPackageAmounts(Map<String, List<Double>> packageAmountPerUnit) {
        this.packageAmounts.clear();

        for(String key : packageAmountPerUnit.keySet()) {
            this.addPackageAmounts(key, packageAmountPerUnit.get(key));
        }
    }

    public PartyType getManufacturer() {
        return this.manufacturer;
    }

    public void setManufacturer(PartyType manufacturer) {
        this.manufacturer = manufacturer;
    }

    public List<BaseMetadataObject> getClassification() {
        if (this.classification == null) {
            this.classification = new ArrayList<>();
        }

        return this.classification;
    }

    public void addClassification(BaseMetadataObject c) {
        this.getClassification().add(c);
    }

    public void setClassification(List<BaseMetadataObject> classification) {
        this.classification = classification;
    }

    public List<String> getClassificationUri() {
        return this.classificationUri;
    }

    public void setClassificationUri(List<String> classificationUri) {
        this.classificationUri = classificationUri;
    }

    public Collection<String> getImgageUri() {
        return this.imgageUri;
    }

    public void setImgageUri(Collection<String> imgageUri) {
        this.imgageUri = imgageUri;
    }

    public Map<String, PropertyType> getCustomProperties() {
        return this.customProperties;
    }

    public void setCustomProperties(Map<String, PropertyType> customProperties) {
        this.customProperties = customProperties;
    }

    private String dynamicKey(String keyVal, Map<String, String> keyMap) {
        String key = dynamicFieldPart(keyVal);
        keyMap.put(key, keyVal);
        return key;
    }

    private String dynamicKey(Map<String, String> keyMap, String... keyPart) {
        String key = dynamicFieldPart(keyPart);
        keyMap.put(key, String.join("@", keyPart));
        return key;
    }

    public static String dynamicFieldPart(String qualifier) {
        if (!StringUtils.hasText(qualifier)) {
            return "undefined";
        } else {
            String dynamicFieldPart = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, qualifier);
            dynamicFieldPart = dynamicFieldPart.replaceAll("[^a-zA-Z0-9_ ]", "");
            dynamicFieldPart = dynamicFieldPart.trim().replaceAll(" ", "_").toUpperCase();
            dynamicFieldPart = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, dynamicFieldPart);
            return dynamicFieldPart;
        }
    }

    public static String dynamicFieldPart(String... strings) {
        List<String> parts = new ArrayList<>();

        for(String part : strings) {
            parts.add(dynamicFieldPart(part));
        }

        return dynamicFieldPart(String.join("_", parts));
    }

    @JsonIgnore
    public Map<String, String> getPropertyMap() {
        return this.propertyMap;
    }

    public Collection<String> getIncoterms() {
        return this.incoterms;
    }

    public void setIncoterms(Collection<String> incoterms) {
        this.incoterms = incoterms;
    }

    public Double getMinimumOrderQuantity() {
        return this.minimumOrderQuantity;
    }

    public void setMinimumOrderQuantity(Double minimumOrderQuantity) {
        this.minimumOrderQuantity = minimumOrderQuantity;
    }

    public Double getWarrantyValidityPeriod() {
        return this.warrantyValidityPeriod;
    }

    public void setWarrantyValidityPeriod(Double warrantyValidityPeriod) {
        this.warrantyValidityPeriod = warrantyValidityPeriod;
    }

    public String getManufactuerItemId() {
        return this.manufactuerItemId;
    }

    public void setManufactuerItemId(String manufactuerItemId) {
        this.manufactuerItemId = manufactuerItemId;
    }

    public String getBaseQuantityUnit() {
        return this.baseQuantityUnits.entrySet().size() > 0 ? this.baseQuantityUnits.entrySet().iterator().next().getValue() : null;
    }

    @SuppressWarnings("unchecked")
    public Double getBaseQuantity() {
        return this.baseQuantities.entrySet().size() > 0 ? this.baseQuantities.entrySet().iterator().next().getValue().get(0) : null;
    }

    public Collection<String> getBaseQuantityUnits() {
        return this.baseQuantityUnits.values();
    }

    public void setBaseQuantityUnits(Collection<String> units) {
        this.baseQuantityUnits.clear();

        for(String unit : units) {
            this.dynamicKey(unit, this.baseQuantityUnits);
        }
    }

    public void addBaseQuantity(String unit, List<Double> amounts) {
        this.baseQuantities.put(this.dynamicKey(unit, this.baseQuantityUnits), amounts);
    }

    @SuppressWarnings("unchecked")
    public Map<String, List<Double>> getBaseQuantities() {
        Map<String, List<Double>> result = new HashMap<>();

        for(String dynUnitKey : this.baseQuantityUnits.keySet()) {
            result.put(this.baseQuantityUnits.get(dynUnitKey), this.baseQuantities.get(dynUnitKey));
        }

        return result;
    }

    public void setBaseQuantities(Map<String, List<Double>> baseQuantityAmountPerUnit) {
        this.baseQuantities.clear();

        for(String key : baseQuantityAmountPerUnit.keySet()) {
            this.addBaseQuantity(key, baseQuantityAmountPerUnit.get(key));
        }
    }
}