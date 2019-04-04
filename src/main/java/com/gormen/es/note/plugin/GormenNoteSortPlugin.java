package com.gormen.es.note.plugin;

import org.elasticsearch.common.Nullable;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.ScriptPlugin;
import org.elasticsearch.script.AbstractDoubleSearchScript;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.NativeScriptFactory;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by gormenhou on 2019/4/3.
 */
public class GormenNoteSortPlugin extends Plugin implements ScriptPlugin{

    public List<NativeScriptFactory> getNativeScripts() {
        return Collections.singletonList((NativeScriptFactory) new GormenNativeScriptFactory());
    }

    public static class GormenNativeScriptFactory implements NativeScriptFactory {

        public ExecutableScript newScript(@Nullable Map<String, Object> map) {
            return new GormenNativeScript();
        }

        public boolean needsScores() {
            return false;
        }

        public String getName() {
            return "gormen_es_note_sort_script";
        }
    }

    public static class GormenNativeScript extends AbstractDoubleSearchScript {
        /**
         *
         * 热度：浏览数1+评论数（含回复）*2+点赞数*3+转发数*4+收藏数*5+笔记发布时间离当前的分钟数X*1800*NN%
         其中，笔记发布时间离当前分钟数X分为：1分钟内、2-3分钟、4-10分钟、11分钟以上；
         对应的权重NN%为：300%、50%、15%、5%；

         当X大于10时，去除基数1800
         当X小于等于1分钟时，发布时间权重计算公式为：1*1800*300%
         当X为2≤X≤3分钟时，发布时间权重计算公式为：（10-X）/2*1800*50%
         当X为4≤X≤10分钟时，发布时间权重计算公式为：（10-X）/2*1800*15%
         当X为X＞10分钟时，发布时间权重计算公式恒为：1800*5%，不再受X增大的影响
         随着发布时间的久远，发布时间对排序的影响不断减弱；其中优先要突出的是发布时间最近的尽可能曝光.

         * @return
         */
        @Override
        public double runAsDouble() {
            long timeNow = System.currentTimeMillis();
            // 笔记创建时候的时间戳
            String timeCreatedStr = source().get("createdTime").toString();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            long timeCreated = 0;
            try {
                timeCreated = sdf.parse(timeCreatedStr).getTime();
            } catch(Exception e) {
            }
            //long timeCreated = ((Date)source().get("createdTime")).getTime();

            int readsCountReal = (Integer)source().get("readsCountReal");
            int commentsCountReal = (Integer)source().get("commentsCountReal");
            int likesCountReal = (Integer)source().get("likesCountReal");
            int sharesCount = (Integer)source().get("sharesCount");
            int favoritesCount = (Integer)source().get("favoritesCount");
            int recommendation = (Integer)source().get("recommendation");

            // 时间权重
            double timePers = 1;
            // 取当前时间和笔记创建时间的差值，单位是 分钟
            long timeDistance = (timeNow - timeCreated) / 60000;
            // 发布时间距离当前的分钟数对应的权重，默认是300
            int timePriority = 300;
            switch (timePriority) {
                case 0:
                case 1:
                    timePers = 5400;
                    break;
                case 2:
                case 3:
                    timePers = ( 10 - timeDistance) * 450;
                    timePriority = 50;
                    break;
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                    timePers = ( 10 - timeDistance) * 135;
                    timePriority = 15;
                    break;
                default:
                    timePers = 90;
                    timePriority = 5;
                    break;
            }
            double hotNum = readsCountReal * 1 +
                    commentsCountReal * 2 +
                    likesCountReal * 3 +
                    sharesCount * 4 +
                    favoritesCount * 5 +
                    timePers;
            return hotNum * timePers;
        }
    }
}
