package cn.sparrowmini.owl.solr.model;


public interface ICatalogueItem extends IMetadataObject {
    String COLLECTION = "item";
    String ID_FIELD = "id";
    String TYPE_FIELD = "doctype";
    String TYPE_VALUE = "item";
    String CATALOGUE_ID_FIELD = "catalogueId";
    String CURRENCY_FIELD = "*_currency";
    String PRICE_FIELD = "*_price";
    String IMAGE_URI_FIELD = "image";
    String FREE_OF_CHARGE_FIELD = "freeOfCharge";
    String PRICE_HIDDEN_FIELD = "priceHidden";
    String CUSTOMIZABLE_FIELD = "customizable";
    String SPARE_PART_FIELD = "sparePart";
    String CERTIFICATE_TYPE_FIELD = "certificateType";
    String CIRCULAR_ECONOMY_CERTIFICATE_FIELD = "circularEconomyCertificates";
    String PERMITTED_PARTIES_FIELD = "permittedParties";
    String RESTRICTED_PARTIES_FIELD = "restrictedParties";
    String APPLICABLE_COUNTRIES_FIELD = "applicableCountries";
    String ESTIMATED_DELIVERY_TIME_FIELD = "*_deliveryTime";
    String ESTIMATED_DELIVERY_TIME_UNIT_FIELD = "*_deliveryTimeUnit";
    String MANUFACTURER_ID_FIELD = "manufacturerId";
    String CREATION_DATE_FIELD = "creationDate";
    String MANUFACTURER_ITEM_ID_FIELD = "manufacturerItemId";
    String SERVICE_TYPE_FIELD = "serviceType";
    String SUPPORTED_PRODUCT_NATURE_FIELD = "supportedProductNature";
    String SUPPORTED_CARGO_TYPE_FIELD = "supportedCargoType";
    String PACKAGE_UNIT_FIELD = "*_packageUnit";
    String PACKAGE_AMOUNT_FILED = "*_package";
    String COMMODITY_CLASSIFICATION_LABEL_FIELD = "commodityClassificationLabel";
    String COMMODITY_CLASSIFICATION_URI_FIELD = "commodityClassficationUri";
    String COMMODITY_CLASSIFICATION_MIX_FIELD = "commodityClassficationMix";
    String TOTAL_CAPACITY_FIELD = "totalCapacity";
    String Total_CAPACITY_UNIT_FIELD = "totalCapacityUnit";
    String TRANSPORT_MODE = "transportMode";
    String EMISSION_TYPE_FIELD = "emissionType";
    String EMISSION_STANDARD_FIELD = "emissionStandard";
    String ESTIMATED_DURATION_FIELD = "estimatedDuration";
    String WARRANTY_VALIDITY_PERIOD_FIELD = "warrantyValidityPeriod";
    String MINIMUM_ORDER_QUANTITY_FIELD = "minimumOrderQuantity";
    String INCOTERMS_FIELD = "incoterm";
    String VALUE_QUALIFIER_FIELD = "valueQualifier";
    String BASE_QUANTITY_UNIT_FIELD = "*_baseQuantityUnit";
    String BASE_QUANTITY_FIELD = "*_baseQuantity";
    String QUALIFIED_KEY_FIELD = "*_key";
    String QUALIFIED_STRING_FIELD = "*_svalues";
    String QUALIFIED_DOUBLE_FIELD = "*_dvalues";
    String QUALIFIED_BOOLEAN_FIELD = "*_bvalue";

    static boolean isQualifiedDynamic(String string) {
        switch (string) {
            case "*_bvalue":
            case "*_dvalues":
            case "*_svalues":
            case "*_key":
                return true;
            default:
                return false;
        }
    }

    static boolean isFixedDynamic(String string) {
        switch (string) {
            case "*_package":
            case "*_packageUnit":
            case "*_currency":
            case "*_price":
            case "*_deliveryTime":
            case "*_label":
            case "*_desc":
            case "*_alternate":
            case "*_hidden":
            case "*_txt":
                return true;
            default:
                return false;
        }
    }
}
