package com.example.natixis_dev.test.DoYouDreamUpServices;

/**
 * Created by natixis-dev on 09/06/2017.
 */

public class DYDUHistoryResponse {

    private String type;
    private Values values;

    public class Values {

        private String dialog;
        private String contextId;
        private long serverTime;
        private Interaction[] interactions;

        public class Interaction {

            private String date;
            private String from; //many values possible
            private String text; //bot response
            private String type;
            private String user; //user question
        }
    }
}
