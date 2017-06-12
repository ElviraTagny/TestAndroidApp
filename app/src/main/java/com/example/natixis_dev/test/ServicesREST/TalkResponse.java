package com.example.natixis_dev.test.ServicesREST;

/**
 * Created by natixis-dev on 09/06/2017.
 */

public class TalkResponse {

    private String type;
    private Values values;

    public class Values
    {
        private int gender;
        private String contextId;
        private boolean hasProfilePicture;
        private boolean keepPopinMinimized;
        private String operatorName;
        private boolean askFeedback;
        private long pollTime;
        private String typeResponse;
        private String operatorExternalId;
        private long serverTime;
        private String text;
        private boolean startLivechat;
        private boolean human;

        public String getText() {
            return text;
        }
    }

    public String getType() {
        return type;
    }

    public Values getValues() {
        return values;
    }
}
