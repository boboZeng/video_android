package com.video.live;


/**
 * fileDesc
 * <p>
 * Created by ribory on 2019-12-05.
 **/
public interface VideoConstants {

    public interface VideoCustomStatus {
        int BUFFERING_TIMEOUT = 1;//缓冲超时
    }

    public interface VideoWhat {
        int WHAT_BUFFERING = 1;//缓冲超时消息
    }

    public interface NETWORK_CLASS {
        /**
         * Network Class Definitions.
         * Do not change this order, it is used for sorting during emergency calling in
         * {@link TelephonyConnectionService#getFirstPhoneForEmergencyCall()}. Any newer technologies
         * should be added after the current definitions.
         */
        /**
         * Unknown network class. {@hide}
         */
        public static final int NETWORK_CLASS_UNKNOWN = 0;
        /**
         * Class of broadly defined "2G" networks. {@hide}
         */
        public static final int NETWORK_CLASS_2_G = 1;
        /**
         * Class of broadly defined "3G" networks. {@hide}
         */
        public static final int NETWORK_CLASS_3_G = 2;
        /**
         * Class of broadly defined "4G" networks. {@hide}
         */
        public static final int NETWORK_CLASS_4_G = 3;
    }



    public interface NETWORK_TYPE {
        /*
         * When adding a network type to the list below, make sure to add the correct icon to
         * MobileSignalController.mapIconSets().
         * Do not add negative types.
         */
        /**
         * Network type is unknown
         */
        public static final int NETWORK_TYPE_UNKNOWN = 0;// TelephonyProtoEnums.NETWORK_TYPE_UNKNOWN; // = 0.
        /**
         * Current network is GPRS
         */
        public static final int NETWORK_TYPE_GPRS = 1;//TelephonyProtoEnums.NETWORK_TYPE_GPRS; // = 1.
        /**
         * Current network is EDGE
         */
        public static final int NETWORK_TYPE_EDGE = 2;// TelephonyProtoEnums.NETWORK_TYPE_EDGE; // = 2.
        /**
         * Current network is UMTS
         */
        public static final int NETWORK_TYPE_UMTS = 3;// TelephonyProtoEnums.NETWORK_TYPE_UMTS; // = 3.
        /**
         * Current network is CDMA: Either IS95A or IS95B
         */
        public static final int NETWORK_TYPE_CDMA = 4;// TelephonyProtoEnums.NETWORK_TYPE_CDMA; // = 4.
        /**
         * Current network is EVDO revision 0
         */
        public static final int NETWORK_TYPE_EVDO_0 = 5;// TelephonyProtoEnums.NETWORK_TYPE_EVDO_0; // = 5.
        /**
         * Current network is EVDO revision A
         */
        public static final int NETWORK_TYPE_EVDO_A = 6;// TelephonyProtoEnums.NETWORK_TYPE_EVDO_A; // = 6.
        /**
         * Current network is 1xRTT
         */
        public static final int NETWORK_TYPE_1xRTT = 7;//TelephonyProtoEnums.NETWORK_TYPE_1XRTT; // = 7.
        /**
         * Current network is HSDPA
         */
        public static final int NETWORK_TYPE_HSDPA = 8;//TelephonyProtoEnums.NETWORK_TYPE_HSDPA; // = 8.
        /**
         * Current network is HSUPA
         */
        public static final int NETWORK_TYPE_HSUPA = 9;//TelephonyProtoEnums.NETWORK_TYPE_HSUPA; // = 9.
        /**
         * Current network is HSPA
         */
        public static final int NETWORK_TYPE_HSPA = 10;// TelephonyProtoEnums.NETWORK_TYPE_HSPA; // = 10.
        /**
         * Current network is iDen
         */
        public static final int NETWORK_TYPE_IDEN = 11;//TelephonyProtoEnums.NETWORK_TYPE_IDEN; // = 11.
        /**
         * Current network is EVDO revision B
         */
        public static final int NETWORK_TYPE_EVDO_B = 12;//TelephonyProtoEnums.NETWORK_TYPE_EVDO_B; // = 12.
        /**
         * Current network is LTE
         */
        public static final int NETWORK_TYPE_LTE = 13;// TelephonyProtoEnums.NETWORK_TYPE_LTE; // = 13.
        /**
         * Current network is eHRPD
         */
        public static final int NETWORK_TYPE_EHRPD = 14;// TelephonyProtoEnums.NETWORK_TYPE_EHRPD; // = 14.
        /**
         * Current network is HSPA+
         */
        public static final int NETWORK_TYPE_HSPAP = 15;// TelephonyProtoEnums.NETWORK_TYPE_HSPAP; // = 15.
        /**
         * Current network is GSM
         */
        public static final int NETWORK_TYPE_GSM = 16;// TelephonyProtoEnums.NETWORK_TYPE_GSM; // = 16.
        /**
         * Current network is TD_SCDMA
         */
        public static final int NETWORK_TYPE_TD_SCDMA = 17;//TelephonyProtoEnums.NETWORK_TYPE_TD_SCDMA; // = 17.
        /**
         * Current network is IWLAN
         */
        public static final int NETWORK_TYPE_IWLAN = 18;// TelephonyProtoEnums.NETWORK_TYPE_IWLAN; // = 18.
        /**
         * Current network is LTE_CA {@hide}
         */
        public static final int NETWORK_TYPE_LTE_CA = 19;//TelephonyProtoEnums.NETWORK_TYPE_LTE_CA; // = 19.

        /**
         * Max network type number. Update as new types are added. Don't add negative types. {@hide}
         */
        public static final int MAX_NETWORK_TYPE = NETWORK_TYPE_LTE_CA;
    }
}
