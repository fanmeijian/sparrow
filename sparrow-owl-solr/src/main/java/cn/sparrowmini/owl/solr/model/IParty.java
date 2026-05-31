package cn.sparrowmini.owl.solr.model;

public interface IParty {
    String COLLECTION = "party";
    String ID_FIELD = "id";
    String ID_SORTED_FIELD = "idSorted";
    String BRAND_NAME_FIELD = "*_brandName";
    String LEGAL_NAME_FIELD = "legalName";
    String LOWERCASE_LEGAL_NAME_FIELD = "lowercaseLegalName";
    String NAME_FIELD = "name";
    String NAME_ML_FIELD = "*_name";
    String ORIGIN_FIELD = "*_origin";
    String CERTIFICATE_TYPE_FIELD = "certificateType";
    String CIRCULAR_ECONOMY_CERTIFICATE_FIELD = "circularEconomyCertificates";
    String PPAP_COMPLIANCE_LEVEL_FIELD = "ppapComplianceLevel";
    String PPAP_DOCUMENT_TYPE_FIELD = "*_ppapDocumentType";
    String TRUST_SCORE_FIELD = "trustScore";
    String TRUST_RATING_FIELD = "trustRating";
    String TRUST_TRADING_VOLUME_FIELD = "trustTradingVolume";
    String TRUST_SELLLER_COMMUNICATION_FIELD = "trustSellerCommunication";
    String TRUST_FULFILLMENT_OF_TERMS_FIELD = "trustFullfillmentOfTerms";
    String TRUST_DELIVERY_PACKAGING_FIELD = "trustDeliveryPackaging";
    String TRUST_NUMBER_OF_TRANSACTIONS_FIELD = "trustNumberOfTransactions";
    String TRUST_NUMBER_OF_EVALUATIONS_FIELD = "trustNumberOfEvaluations";
    String LOGO_ID_FIELD = "logoId";
    String BUSINESS_TYPE_FIELD = "businessType";
    String ACTIVITY_SECTORS_FIELD = "*_activitySectors";
    String BUSINESS_KEYWORDS_FIELD = "*_businessKeywords";
    String VERIFIED_FIELD = "verified";
    String WEBSITE_FIELD = "website";
    String LOCATION_LONGITUDE = "locationLongitude";
    String LOCATION_LATITUDE = "locationLatitude";
    String BASEPLATFORM_COMPANY_FIELD = "basePlatformCompanyId";
}
