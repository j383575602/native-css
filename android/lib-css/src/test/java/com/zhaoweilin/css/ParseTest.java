package com.zhaoweilin.css;

import com.zhaoweilin.css.CssDrawable;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ParseTest {

    @Test
    public void test1() {
        String css = "{shape:rect;fill:sl(grad(#abcd21,#78723a,1)@1,grad(#abcd21,#78723a,1)@2);stroke-color:sl(grad(#123122)@3,grad(#123122)@4);stroke-width:1px;stroke-style:dash;corner:2 2 2 2}";
        CssDrawable.Css style = CssDrawable.CssParser.parse(css);
        Assert.assertNotNull(style);
        System.out.println(style.toString());
    }

    @Test
    public void test2() {
        String css = "{shape:rect;fill:sl(grad(#abcd21,#78723a,1)@1,grad(#abcd21,#78723a,1)@2);stroke-color:sl(#123122@1,#123122@2);stroke-width:1px;stroke-style:dash;corner:2 2 2 2}";
        CssDrawable.Css style = CssDrawable.CssParser.parse(css);
        Assert.assertNotNull(style);
        System.out.println(style.toString());
    }

    @Test
    public void test3() {
        String css = "{shape:rect;fill:#878987;stroke-color:#123122;stroke-width:1px;stroke-style:dash;corner:2 2 2 2}";
        CssDrawable.Css style = CssDrawable.CssParser.parse(css);
        Assert.assertNotNull(style);
        System.out.println(style.toString());
    }

    @Test
    public void test4() {
        String css = "{shape:rect;fill:url(http\\://ctrip.com);stroke-color:#123122;stroke-width:1px;stroke-style:dash;corner:2 2 2 2;width:100;height:100}";
        CssDrawable.Css style = CssDrawable.CssParser.parse(css);
        Assert.assertNotNull(style);
        System.out.println(style.toString());
    }
}