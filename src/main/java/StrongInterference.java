import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.awt.Font;

import com.wf.captcha.ArithmeticCaptcha;

class StrongInterference extends ArithmeticCaptcha {

    static Map<Character, String> numberMap = new HashMap<>();
    static {
        numberMap.put('0', "零");
        numberMap.put('1', "壹");
        numberMap.put('2', "贰");
        numberMap.put('3', "叁");
        numberMap.put('4', "肆");
        numberMap.put('5', "伍");
        numberMap.put('6', "陆");
        numberMap.put('7', "柒");
        numberMap.put('8', "捌");
        numberMap.put('9', "玖");
    }

    // 定义运算符和中文的对应关系
    static Map<Character, String> operatorMap = new HashMap<>();
    static {
        operatorMap.put('+', "加");
        operatorMap.put('-', "减");
        operatorMap.put('x', "乘");
        operatorMap.put('/', "除");
    }

    static List<String> CHINESE_FONT_NAMES = new ArrayList<>();
    static {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = ge.getAvailableFontFamilyNames();
        for (String fontName : fontNames) {
            Font font = new Font(fontName, Font.PLAIN, 12);
            // 检查字体是否支持显示"汉"或"文"，这两个字符足以测试中文支持
            if (font.canDisplayUpTo("零") == -1) {
                CHINESE_FONT_NAMES.add(fontName);
            }
        }
        System.out.println("当前系统可用的字体有：" + CHINESE_FONT_NAMES);
    }

    private Boolean useChinese;

    Random rand = new Random();

    public void setUseChinese(Boolean useChinese) {
        this.useChinese = useChinese;
    }

    public String text() {
        return getArithmeticString();
    }

    public static String getRandomFontName() {
        Random random = new Random();
        int randomIndex = random.nextInt(CHINESE_FONT_NAMES.size()); // 随机选择一个索引
        return CHINESE_FONT_NAMES.get(randomIndex); // 返回随机选中的字体名称
    }

    @Override
    protected char[] alphas() {
        char[] res = super.alphas();
        if (useChinese) {
            setArithmeticString(convertToChinese(getArithmeticString()));
        }
        return res;
    }

    private static String convertToChinese(String arithmeticExpression) {
        arithmeticExpression = arithmeticExpression.replace("=?", "");
        // 转换算术表达式为中文表述
        StringBuilder chineseExpression = new StringBuilder();
        for (char ch : arithmeticExpression.toCharArray()) {
            if (numberMap.containsKey(ch)) {
                chineseExpression.append(numberMap.get(ch));
            } else if (operatorMap.containsKey(ch)) {
                chineseExpression.append(operatorMap.get(ch));
            } else {
                chineseExpression.append(ch);
            }
        }
        return chineseExpression.toString();
    }

    public StrongInterference(int width, int height) {
        super(width, height);
        this.useChinese = Boolean.FALSE;
    }

    public StrongInterference(int width, int height, int len) {
        this(width, height);
        this.useChinese = Boolean.FALSE;
        setLen(len);
    }

    public StrongInterference(int width, int height, int len, Font font) {
        this(width, height, len);
        this.useChinese = Boolean.FALSE;
        setFont(font);
    }

    /**
     * 生成验证码
     *
     * @param out 输出流
     * @return 是否成功
     */
    @Override
    public boolean out(OutputStream out) {
        checkAlpha();
        return graphicsImage(getArithmeticString().toCharArray(), out);
    }

    @Override
    public String toBase64() {
        return toBase64("data:image/png;base64,");
    }

    /**
     * 生成验证码图形
     *
     * @param strs 验证码
     * @param out  输出流
     * @return boolean
     * @throws FontFormatException
     */
    private boolean graphicsImage(char[] strs, OutputStream out) {
        try {
            BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = (Graphics2D) bi.getGraphics();
            // 填充背景
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);
            // 抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // 画干扰圆
            drawOval(2, g2d);
            drawBesselLine(4, g2d);
            // 画字符串
            try {
                if (!this.useChinese) {
                    setFont(rand.nextInt(10), rand.nextInt(10) + 70);
                    g2d.setFont(getFont());
                } else {
                    String fontName = getRandomFontName();
                    System.out.println("Randomly selected font name: " + fontName);

                    g2d.setFont(new Font(fontName, Font.BOLD, 45));
                }

            } catch (Exception e) {
            }

            FontMetrics fontMetrics = g2d.getFontMetrics();
            int fW = width / strs.length; // 每一个字符所占的宽度
            int fSp = (fW - (int) fontMetrics.getStringBounds("8", g2d).getWidth()) / 2; // 字符的左右边距
            for (int i = 0; i < strs.length; i++) {
                g2d.setColor(color());
                int fY = height
                        - ((height - (int) fontMetrics.getStringBounds(String.valueOf(strs[i]), g2d).getHeight()) >> 1); // 文字的纵坐标
                g2d.drawString(String.valueOf(strs[i]), i * fW + fSp + 3,
                        fY - 3 + rand.nextInt(height / 3) - height / 6);
            }

            // 正弦曲线参数
            double frequency = 2; // 曲线的频率
            double amplitude = rand.nextInt(10) + 40; // 曲线的振幅
            double phase = rand.nextDouble() * 2 * Math.PI; // 曲线的相位
            int line_width = (int) ((rand.nextDouble() * 10) + 10); // 曲线的宽度
            int y_offset = rand.nextInt(width / 3) - width / 6; // y轴偏移量

            // 生成正弦曲线
            for (int x = 1; x < width; x++) {
                int y = (int) (height / 2 + amplitude * Math.sin((2 * Math.PI * frequency * x / width) + phase))
                        + y_offset;
                g2d.drawLine(x - line_width, y, x + line_width, y); // 绘制从上一个点到当前点的线条
            }

            for (int i = 0; i < 10; i++) { // 数字的数量，可以根据需要调整
                int x = rand.nextInt(width);
                int y = rand.nextInt(height);
                g2d.setColor(new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), 0.1f)); // 设置随机颜色和透明度
                g2d.drawString(String.valueOf(rand.nextInt(10)), x, y); // 在随机位置绘制数字
            }

            g2d.dispose();
            ImageIO.write(bi, "png", out);
            out.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}