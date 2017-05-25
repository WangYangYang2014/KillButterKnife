package com.maihaoche.cx5;

import com.intellij.openapi.editor.Document;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yang on 17/5/15.
 */
public class CX5Handler {

    /**
     * 匹配诸如：
     *
     * @BindView(R.id.view_adopt) View mAdoptBtn;
     * 包括R.id.xxx  和 R2.id.xxx
     * 一共有三个子分组，例子中依次输出 view_adopt， View, mAdoptBtn.
     * 注意：子分组从group(1)开始，group(0)输出的是整个匹配项。
     */
    public static final String FIND_VIEW = "((@BindView)+\\(R2?\\.id\\." +
            //资源id
            "(\\w+)" +
            "\\)[\\s]+)" +
            //其他注解
            "(@\\w+[\\s]+)?" +
            //可见域
            "(public|protected)?[\\s]*" +
            //类型
            "(\\w+)[\\s]+" +
            //变量名字
            "((\\w+)" +
            ";)";
    private static final Pattern FIND_VIEW_PATTERN = Pattern.compile(CX5Handler.FIND_VIEW);

    public static final String ONLICK = "((@OnClick)+\\(R2?\\.id\\." +
            //资源id
            "(\\w+)" +
            "\\)[\\s]+)" +
            //可见域
            "(public|protected)?[\\s]*" +
            "(void)+[\\s]+" +
            //函数名
            "(\\w+)" +
            "\\(+(View[\\s]*\\w+)?\\)+[\\s]*\\{+";
    private static final Pattern ONCLICK_PATTERN = Pattern.compile(CX5Handler.ONLICK);


    /**
     * 获取所有的bindView的数据
     *
     * @param inputStr
     * @return
     */
    public static ArrayList<FindViewInfo> getFindView(String inputStr) {
        ArrayList<FindViewInfo> findViewInfos = new ArrayList<>();
        Matcher matcher = FIND_VIEW_PATTERN.matcher(inputStr);
        while (matcher.find()) {
//            Utils.info("matcher.groupCount():" + matcher.groupCount());
//            for (int i = 0; i < matcher.groupCount(); i++) {
//                Utils.info("matcher.group(" + i + "):" + matcher.group(i));
//            }
//            Utils.info("matcher.group(" + matcher.groupCount() + "):" + matcher.group(matcher.groupCount()));
            if (matcher.groupCount() < 8) {
                throw new IllegalArgumentException("匹配出错，某个文本匹配出来的groupCount为" + matcher.groupCount() + ",小于8.请检查正则表达式或者文本");
            }
            FindViewInfo findViewInfo = new FindViewInfo();
            findViewInfo.mStart = matcher.start();
            findViewInfo.mBindViewRowEnd = matcher.end(1);
            findViewInfo.mEnd = matcher.end(7);
            findViewInfo.mResIdStr = matcher.group(3);
            findViewInfo.mViewTypeStr = matcher.group(6);
            findViewInfo.mVerbNameStr = matcher.group(8);
            findViewInfos.add(findViewInfo);
        }
        return findViewInfos;
    }

    /**
     * 处理findView的文本替换
     *
     * @param document
     * @param bindViewInfos
     * @param target
     * @param orignalStart
     * @return
     */
    public static TextHandleResult handleFindView(Document document, ArrayList<FindViewInfo> bindViewInfos, String target, int orignalStart) {
        TextHandleResult result = new TextHandleResult();
        if (bindViewInfos.size() > 0) {
            String findDeclear = "\n";
            int innerStart = orignalStart;
            for (int i = 0; i < bindViewInfos.size(); i++) {
                FindViewInfo findViewInfo = bindViewInfos.get(i);
                findDeclear += findViewInfo.mVerbNameStr + "="
                        + "(" + findViewInfo.mViewTypeStr + ")" + " "
                        + (target.equals("") ? " " : target + ".") + "findViewById(R.id." + findViewInfo.mResIdStr + ");\n";
                //把@BindView哪一行去掉
                document.replaceString(innerStart + findViewInfo.mStart, innerStart + findViewInfo.mBindViewRowEnd, "\n");
                int thisDelta = 1 - (findViewInfo.mBindViewRowEnd - findViewInfo.mStart);//变化值
                result.mDelta += thisDelta;
                innerStart = innerStart + thisDelta;
            }
            int replaceStart = orignalStart + bindViewInfos.get(0).mStart;
            findDeclear = "\nprivate void findView(" + (target.equals("") ? " " : "View " + target) + ")" +
                    "{" + findDeclear + "}\n";
            document.replaceString(replaceStart, replaceStart, findDeclear);
            result.mDelta += findDeclear.length();
            result.mReplaceStart = replaceStart;
            result.mHandled = true;
            return result;
        }
        return result;
    }

    /**
     * 获取点击的信息
     *
     * @param inputStr
     * @return
     */
    public static ArrayList<OnClickInfo> getOnClick(String inputStr) {
        ArrayList<OnClickInfo> onClickInfos = new ArrayList<>();
        Matcher matcher = ONCLICK_PATTERN.matcher(inputStr);
        while (matcher.find()) {
//            Utils.info("matcher.groupCount():" + matcher.groupCount());
//            for (int i = 0; i < matcher.groupCount(); i++) {
//                Utils.info("matcher.group(" + i + "):" + matcher.group(i));
//            }
//            Utils.info("matcher.group(" + matcher.groupCount() + "):" + matcher.group(matcher.groupCount()));
            if (matcher.groupCount() < 6) {
                throw new IllegalArgumentException("匹配OnClick出错，某个文本匹配出来的groupCount为" + matcher.groupCount() + ",小于6.请检查正则表达式或者文本");
            }
            OnClickInfo onClickInfo = new OnClickInfo();
            onClickInfo.mStart = matcher.start(1);
            onClickInfo.mOnClickRowEnd = matcher.end(1);
            onClickInfo.mResIdStr = matcher.group(3);
            onClickInfo.mFunctionName = matcher.group(6);
            if (matcher.group(7) != null && !matcher.group(7).equals("")) {
                onClickInfo.mUseViewParameter = true;
            }
            onClickInfos.add(onClickInfo);
        }
        return onClickInfos;
    }


    /**
     * 处理@OnClick的文档的替换
     *
     * @param document
     * @param bindViewInfos
     * @param target
     * @param orignalStart
     * @return
     */
    public static TextHandleResult handleOnClick(Document document, ArrayList<OnClickInfo> bindViewInfos, String target, final int orignalStart) {
        TextHandleResult result = new TextHandleResult();
        if (bindViewInfos.size() > 0) {
            String clickDeclear = "\n";
            int innerStart = orignalStart;
            for (int i = 0; i < bindViewInfos.size(); i++) {
                CX5Handler.OnClickInfo onClickInfo = bindViewInfos.get(i);
                clickDeclear += (target.equals("") ? "" : target + ".") + "findViewById(R.id." + onClickInfo.mResIdStr + ").setOnClickListener(v -> " + onClickInfo.mFunctionName + "(" + (onClickInfo.mUseViewParameter ? "v" : "") + "));\n";
                document.replaceString(innerStart + onClickInfo.mStart, innerStart + onClickInfo.mOnClickRowEnd, "\n");
                //由于发生了replace，所以，内部的start要做相应调整
                int thisDelta = 1 - (onClickInfo.mOnClickRowEnd - onClickInfo.mStart);//变化值
                innerStart = innerStart + thisDelta;
                result.mDelta += thisDelta;
            }
            int replaceStart = orignalStart + bindViewInfos.get(0).mStart;
            clickDeclear = "\nprivate void setOnClick(" + (target.equals("") ? "" : "View " + target) + ") {" + clickDeclear + "}\n";
            document.replaceString(replaceStart, replaceStart, clickDeclear);
            result.mDelta += clickDeclear.length();
            result.mReplaceStart = replaceStart;
            result.mHandled = true;
            return result;
        }
        return result;
    }


    /**
     * bindview输出的数据，{@link #FIND_VIEW}
     */
    public static class FindViewInfo extends BaseInfo {
        public String mResIdStr = "";
        public String mViewTypeStr = "";
        public String mVerbNameStr = "";
        //@BindView那一行最后的end
        public int mBindViewRowEnd = 0;
    }

    /**
     * 点击事件
     */
    public static class OnClickInfo extends BaseInfo {
        public String mResIdStr = "";
        public String mFunctionName = "";
        //@OnClick那一行最后的end
        public int mOnClickRowEnd = 0;
        public boolean mUseViewParameter = false;
    }

    public static class BaseInfo {
        public int mStart = 0;
        public int mEnd = 0;
    }

    /**
     * 进程文本处理后的输出结果
     */
    public static class TextHandleResult {
        public int mDelta = 0;
        public int mReplaceStart = 0;
        public boolean mHandled = false;
    }
}
