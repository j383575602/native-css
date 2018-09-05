package com.ctrip.css;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * { } ( ) , : ; @是保留字符,值中存在这些字符需要使用转义字符。<br>
 * sl:表示状态列表statelist。@表示其状态。#123122@2表示状态为2时取该值来展示，状态值为开关，支持多个状态取相同值<br>
 * grad:表示渐变。接受三个参数。开始颜色，终点颜色，方向grad(startColor,endColor,orientation)<br>
 * url:表示一个网络图片资源。暂不支持加载网络图片<br>
 * fill:表示填充值。值可以为渐变，颜色，url图片等值<br>
 * shape:表示形状。值可以是rect oval,默认为rect<br>
 * stroke-width:表示边框宽度。默认单位为dip，可设px<br>
 * stroke-color:表示边框颜色。值可以为渐变，颜色，状态列表<br>
 * stroke-css:表示边框风格。值为颜色或者dash(intervals,phase)<br>
 * corner：表示四角弧度。格式为 lt rt rb lb表示左上，右上，右下，左下弧度。默认单位为DP<br>
 * width：表示宽度。默认单位为dp<br>
 * height：表示高度。默认单位为dp<br>
 *
 * <p>
 * 1、sl表示状态列表。支持的状态为有enabled（1）pressed(2)checked(4)selected(8)activated(16)<br>
 * 正常情况下状态至少都是enabled的。因此需要要在想要的状态上加1。#xxxxxx@3表示enabled+pressed的颜色值
 * <p>
 * 2、grad表示渐变，只支持两个颜色的线性渐变。方向支持8种方向TB(1)RTBL(2)RL(3)RBTL(4)BT(5)LBRT(6)LR(7)LTRB(8)
 * <p>
 * 示例：
 * <p>
 * {shape:rect;fill:sl(grad(#abcd21,#78723a,1)@1,grad(#abcd21,#78723a,1)@2);stroke-color:sl(grad(#123122)@3,grad(#123122)@5);stroke-width:1px;stroke-style:dash;corner:2 2 2 2}<br>
 * {shape:rect;fill:sl(grad(#abcd21,#78723a,1)@1,grad(#abcd21,#78723a,1)@2);stroke-color:sl(#123122@1,#123122@2);stroke-width:1px;stroke-style:dash(8,10,5);corner:2 2 2 2}<br>
 * {shape:rect;fill:#878987;stroke-color:#123122;stroke-width:1px;stroke-style:dash;corner:2 2 2 2}<br>
 * {shape:rect;fill:url(http\\://ctrip.com);stroke-color:#123122;stroke-width:1px;stroke-style:dash;corner:2 2 2 2;width:100;height:100}<br>
 *
 * @author wlzhao
 */
public class CssDrawable extends Drawable {

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private CssRender render = new CssRender();
    public static float sDensity = 3;

    public CssDrawable(String cssString) {
        render.setPaint(paint);
        render.setDensity(sDensity);
        try {
            Css css = CssParser.parse(cssString);
            render.setCss(css);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        render.setBounds(getBounds());
        render.render(canvas, getState());
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getIntrinsicWidth() {
        return render.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return render.getIntrinsicHeight();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public boolean setState(@NonNull int[] stateSet) {
        int[] old = getState();
        if (!arrayEquals(old,stateSet)) {
            super.setState(stateSet);
            invalidateSelf();
            return true;
        } else {
            return false;
        }
    }

    private static boolean arrayEquals(int[] a,int[] b) {
        if (a.length != b.length) {
            return false;
        }
        for(int i=0;i<a.length;i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isStateful() {
        return render.isStateful();
    }

    public static class Css {
        private List<Item> items = new ArrayList<>();
        public static class Item {
            String key;
            Value value;
        }

        public interface Value {

            int getType();

            Object getValue();

            int getStatus();

            void setValue(Object value);

            void setStatus(int status);
        }

        private static class MethodValue implements Value {
            public String name;
            public List<Value> params = new ArrayList<>();
            public int status;

            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();
                sb.append(name).append("(");
                for (int i = 0; i < params.size(); i++) {
                    Value v = params.get(i);
                    sb.append(v.toString());
                    if (v.getStatus() > 0) {
                        sb.append("@").append(v.getStatus());
                    }
                    if (i < params.size() - 1) {
                        sb.append(",");
                    }
                }
                sb.append(")");
                return sb.toString();
            }

            @Override
            public int getType() {
                return 1;
            }

            @Override
            public Object getValue() {
                return params;
            }

            @Override
            public int getStatus() {
                return status;
            }

            @Override
            public void setValue(Object value) {
                params = (List<Value>) value;
            }

            @Override
            public void setStatus(int status) {
                this.status = status;
            }
        }

        public static class StringValue implements Value {
            String value;
            int status;

            @Override
            public int getType() {
                return 0;
            }

            @Override
            public Object getValue() {
                return value;
            }

            @Override
            public int getStatus() {
                return status;
            }

            @Override
            public void setValue(Object value) {
                this.value = String.valueOf(value);
            }

            @Override
            public void setStatus(int status) {
                this.status = status;
            }

            @Override
            public String toString() {
                return this.value;
            }
        }

        public void addItem(Item item) {
            items.add(item);
        }

        public Item getItem(String key) {
            for (Item item : items) {
                if (item.key.equals(key)) {
                    return item;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            for (Item item : items) {
                sb.append(item.key).append(":");
                sb.append(item.value).append(";");
            }
            if (items.size() > 0) {
                sb.setLength(sb.length() - 1);
            }
            sb.append("}");

            return sb.toString();
        }
    }

    public static class CssParser {
        @Nullable
        public static Css parse(String css) {
            try {
                int i = 0;
                Stack<Css.MethodValue> stack = new Stack<>();
                StringBuilder sb = new StringBuilder();
                Css style = null;
                Css.Item item = null;
                int level = 0;
                boolean readStatus = false;
                boolean lastIsSlash = false;
                while (i < css.length()) {
                    boolean tempLastIsSlash = lastIsSlash;
                    lastIsSlash = false;
                    char ch = css.charAt(i);
                    if (tempLastIsSlash) {
                        sb.append(ch);
                        i++;
                        continue;
                    }
                    if ('{' == ch) {
                        style = new Css();
                    } else if ('(' == ch) {
                        Css.MethodValue methodValue = new Css.MethodValue();
                        String name = sb.toString();
                        methodValue.name = name;
                        stack.push(methodValue);
                        sb.setLength(0);
                        level++;
                    } else if (':' == ch) {
                        String key = sb.toString();
                        sb.setLength(0);
                        item = new Css.Item();
                        item.key = key;
                    } else if (';' == ch) {
                        if (sb.length() > 0) {
                            String data = sb.toString();
                            sb.setLength(0);
                            Css.StringValue value = new Css.StringValue();
                            value.value = data;
                            if (item != null) {
                                item.value = value;
                            }
                        }
                        style.addItem(item);
                    } else if (')' == ch) {
                        Css.MethodValue method = stack.pop();
                        if (sb.length() > 0) {
                            if (readStatus) {
                                int status = Integer.parseInt(sb.toString());
                                List<Css.Value> params = method.params;
                                sb.setLength(0);
                                params.get(params.size() - 1).setStatus(status);
                                readStatus = false;
                            } else {
                                Css.StringValue value = new Css.StringValue();
                                value.value = sb.toString();
                                sb.setLength(0);
                                method.params.add(value);
                            }
                        }
                        level--;
                        if (level > 0) {
                            stack.peek().params.add(method);
                        } else {
                            item.value = method;
                        }
                        sb.setLength(0);
                    } else if (',' == ch) {
                        String value = sb.toString();
                        sb.setLength(0);
                        if (readStatus) {
                            int status = Integer.parseInt(value);
                            Css.MethodValue method = stack.peek();
                            List<Css.Value> params = method.params;
                            params.get(params.size() - 1).setStatus(status);
                            readStatus = false;
                        } else if (level > 0) {
                            Css.MethodValue method = stack.peek();
                            Css.StringValue value1 = new Css.StringValue();
                            value1.value = value;
                            method.params.add(value1);
                        }
                    } else if ('@' == ch) {
                        if (sb.length() > 0) {
                            Css.StringValue value = new Css.StringValue();
                            value.value = sb.toString();
                            Css.MethodValue method = stack.peek();
                            method.params.add(value);
                            sb.setLength(0);
                        }
                        readStatus = true;
                    } else if ('}' == ch) {
                        if (sb.length() > 0) {
                            Css.StringValue stringValue = new Css.StringValue();
                            stringValue.value = sb.toString();
                            item.value = stringValue;
                        }
                        style.addItem(item);
                        break;
                    } else if ('\\' == ch) {
                        lastIsSlash = true;
                    } else {
                        sb.append(ch);
                    }
                    i++;
                }
                return style;
            } catch (Exception e) {
                throw new RuntimeException("parse failed! Css String is error");
            }
        }
    }

    private static class CssRender {
        private Paint paint;
        private float density;
        private Rect bounds;
        private Shape shape;
        private Css css;
        private boolean isStateful;
        private int[] state;

        public Rect getBounds() {
            return bounds;
        }

        public void setCss(Css css) {
            this.css = css;
            shape = createShape();
        }

        public void setBounds(Rect bound) {
            this.bounds = bound;
        }

        public void setPaint(Paint paint) {
            this.paint = paint;
        }

        public void setDensity(float density) {
            this.density = density;
        }

        public void render(Canvas canvas, int[] state) {
            this.state = state;
            if (shape == null) {
                return;
            }
            renderFill(canvas);
            renderStroke(canvas);
        }

        private void renderFill(Canvas canvas) {
            paint.reset();
            paint.setAntiAlias(true);
            shape.resize(getBounds().width(), getBounds().height());
            Css.Item fillItem = css.getItem("fill");
            configColor(fillItem, state);
            paint.setStyle(Paint.Style.FILL);
            shape.draw(canvas, paint);
        }

        private void renderStroke(Canvas canvas) {
            paint.reset();
            paint.setAntiAlias(true);
            boolean hasStroke = configStrokeWidth();
            if (!hasStroke) {
                return;
            }
            configStrokeColor();
            configStrokePathEffect();
            shape.draw(canvas,paint);
        }

        private boolean configStrokeWidth() {
            int strokeWidth = 0;
            Css.Item strokeItem = css.getItem("stroke-width");
            if (strokeItem != null) {
                Css.StringValue value = (Css.StringValue) strokeItem.value;
                strokeWidth = parseSize(value.value);
                paint.setStrokeWidth(strokeWidth);
                paint.setStyle(Paint.Style.STROKE);
            }
            return strokeWidth > 0;
        }

        private void configStrokeColor() {
            Css.Item strokeColor = css.getItem("stroke-color");
            configColor(strokeColor,state);
        }

        private void configStrokePathEffect() {
            Css.Item strokeStyle = css.getItem("stroke-style") ;
            if (strokeStyle != null) {
                Css.MethodValue value = (Css.MethodValue) strokeStyle.value;
                if ("dash".equals(value.name)) {
                    int size = value.params.size();
                    float phase = parseSize(value.params.get(size-1).getValue().toString());
                    float[] intervals = new float[size-1];
                    for(int i=0;i<size-1;i++) {
                        intervals[i] = parseSize(value.params.get(i).getValue().toString());
                    }
                    PathEffect effect = new DashPathEffect(intervals,phase);
                    paint.setPathEffect(effect);
                }
            }
        }

        private LinearGradient evalGrad(Css.MethodValue methodValue, int[] state) {
            int[] colors = new int[2];
            List<Css.Value> params = (List<Css.Value>) methodValue.getValue();
            colors[0] = Color.parseColor(params.get(0).getValue().toString());
            colors[1] = Color.parseColor(params.get(1).getValue().toString());
            int orientation = Integer.parseInt(params.get(2).getValue().toString());
            int width = getBounds().width();
            int height = getBounds().height();
            int x0 = 0;
            int y0 = 0;
            int x1 = 0;
            int y1 = 0;

            if (orientation == 1) {
                //top to bottom
                x0 = width /2;
                y0 = 0;
                x1 = width / 2;
                y1 = height;
            } else if (orientation == 2) {
                //top right to bottom left
                x0 = width;
                y0 = 0;
                x1 = 0;
                y1 = height;
            } else if (orientation == 3) {
                //right to left
                x0 = width;
                y0 = height/2;
                x1 = 0;
                y1 = height / 2;
            } else if (orientation == 4) {
                //bottom right to top left
                x0 = width;
                y0 = height;
                x1 = 0;
                y1 = 0;
            } else if (orientation == 5) {
                //bottom to top
                x0 = width /2;
                y0 = height;
                x1 = width / 2;
                y1 = 0;
            } else if (orientation == 6) {
                //bottom left to top right
                x0 = 0;
                y0 = height;
                x1 = width;
                y1 = 0;
            } else if (orientation == 7) {
                //left to right
                x0 = 0;
                y0 = height/2;
                x1 = width;
                y1 = height/2;
            } else if (orientation == 8) {
                //top left to bottom right
                x0 = 0;
                y0 = 0;
                x1 = width;
                y1 = height;
            }
            LinearGradient linearGradient = new LinearGradient(x0,y0,x1,y1,colors,null, Shader.TileMode.REPEAT);
            return linearGradient;
        }

        private void configColor(Css.Item colorItem, int[] state) {
            if (colorItem != null) {
                if (colorItem.value instanceof Css.StringValue) {
                    Css.StringValue stringValue = (Css.StringValue) colorItem.value;
                    int color = Color.parseColor(stringValue.value);
                    paint.setColor(color);
                    paint.setShader(null);
                } else if (colorItem.value instanceof Css.MethodValue) {
                    Css.MethodValue methodValue = (Css.MethodValue) colorItem.value;
                    if ("sl".equals(methodValue.name)) {
                        isStateful = true;
                        List<Css.Value> values = methodValue.params;
                        int [] status = state;
                        int indexValue = 0;

                        for(int i =0;i<status.length;i++) {
                            if (status[i] == android.R.attr.state_enabled) {
                                indexValue |= 1;
                            } else if (status[i] == android.R.attr.state_pressed) {
                                indexValue |= 2;
                            } else if (status[i] == android.R.attr.state_checked) {
                                indexValue |= 4;
                            } else if (status[i] == android.R.attr.state_selected) {
                                indexValue |= 8;
                            } else if (status[i] == android.R.attr.state_activated) {
                                indexValue |= 16;
                            }
                        }

                        for(Css.Value value : values) {
                            if ((value.getStatus() == indexValue)) {
                                if (value instanceof Css.StringValue) {
                                    Css.StringValue stringValue = (Css.StringValue) colorItem.value;
                                    int color = Color.parseColor(stringValue.value);
                                    paint.setColor(color);
                                } else if (value instanceof Css.MethodValue){
                                    if ("grad".equals(((Css.MethodValue) value).name)) {
                                        Css.MethodValue methodValue1 = (Css.MethodValue) value;
                                        LinearGradient linearGradient = evalGrad(methodValue1,state);
                                        paint.setShader(linearGradient);
                                    }
                                }
                                break;
                            }
                        }
                    } else if ("url".equals(methodValue.name)) {

                    } else if ("grad".equals(methodValue.name)) {
                        LinearGradient linearGradient = evalGrad(methodValue,state);
                        paint.setShader(linearGradient);
                    }
                }
            }
        }

        public int parseSize(String value) {
            String lower = value.toLowerCase();
            int indexPX = lower.indexOf("px");
            int indexDIP = lower.indexOf("dip");
            int indexDP = lower.indexOf("dp");

            if (indexDIP < 0 && indexPX < 0 && indexDP < 0) {
                return (int)((Integer.parseInt(value) * density) + 0.5f);
            }
            if (indexDIP > 0) {
                return (int)((Integer.parseInt(value.substring(0,indexDIP)) * density) + 0.5f);
            }

            if (indexDP > 0) {
                return (int)((Integer.parseInt(value.substring(0,indexDP)) * density) + 0.5f);
            }

            return Integer.parseInt(value.substring(0,indexPX));
        }

        public int getIntrinsicWidth() {
            if (css == null) {
                return 0;
            }
            Css.Item widthItem = css.getItem("width");
            if (widthItem != null) {
                Css.StringValue stringValue = (Css.StringValue) widthItem.value;
                return  parseSize(stringValue.value);
            }
            return 0;
        }

        public int getIntrinsicHeight() {
            if (css == null) {
                return 0;
            }
            Css.Item heightItem = css.getItem("height");
            if (heightItem != null) {
                Css.StringValue stringValue = (Css.StringValue) heightItem.value;
                return parseSize(stringValue.value);
            }
            return 0;
        }

        private Shape createShape() {
            if (css == null) {
                return null;
            }
            Css.Item shapeItem = css.getItem("shape");
            boolean isOval = false;
            if (shapeItem == null) {
                shape = new RectShape();
            } else {
                Css.StringValue stringValue = (Css.StringValue) shapeItem.value;
                if ("oval".equals(stringValue.value)) {
                    shape = new OvalShape();
                    isOval = true;
                } else {
                    shape = new RectShape();
                }
            }
            Css.Item cornerItem = css.getItem("corner");
            if (!isOval && cornerItem != null) {
                Css.StringValue stringValue = (Css.StringValue) cornerItem.value;
                String corner = stringValue.value;
                String[] corners = corner.split(" ");
                float[] cornerfs = new float[corners.length];
                boolean anyValue = false;
                for(int i=0;i<corners.length;i++) {
                    cornerfs[i] = parseSize(corners[i]);
                    if (cornerfs[i] > 0) {
                        anyValue = true;
                    }
                }
                if (anyValue) {
                    float[] outer = {cornerfs[0],cornerfs[0],cornerfs[1],cornerfs[1],cornerfs[2],cornerfs[2],cornerfs[3],cornerfs[3]};
                    shape = new RoundRectShape(outer,null,null);
                }
            }
            return shape;
        }
        public boolean isStateful() {
            return isStateful;
        }
    }
}