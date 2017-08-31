package com.tagny.dev.test.DoYouDreamUpServices;

import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.style.StrikethroughSpan;
import android.util.Log;

import org.xml.sax.XMLReader;

/**
 * Created by tagny on 13/06/2017.
 */

public class CustomTagHandler implements Html.TagHandler {

        public void handleTag(boolean opening, String tag, Editable output,
                XMLReader xmlReader) {
            if(tag.equals("ul") && !opening) output.append("\n");
            if(tag.equals("li") && opening) output.append("\n\t• ");
        }

}
