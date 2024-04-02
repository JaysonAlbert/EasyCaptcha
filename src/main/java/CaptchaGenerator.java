
import com.wf.captcha.base.Captcha;
import java.io.FileOutputStream;
import java.io.IOException;


import java.awt.FontFormatException;
import java.io.File;

public class CaptchaGenerator {

    public static void main(String[] args) throws IOException, FontFormatException {
        FileOutputStream labelStream = new FileOutputStream(new File("/home/wangjie/PycharmProjects/dddd_trainer/projects/piaoxingqiu/datasets/labels.txt"));

        for(int i = 0; i < 30000; i++){
            String image_name = String.format("/home/wangjie/PycharmProjects/dddd_trainer/projects/piaoxingqiu/datasets/images/%d.png", i);
            FileOutputStream outputStream = new FileOutputStream(new File(image_name));
            StrongInterference captcha = new StrongInterference(255, 132);
            captcha.setLen(2);  // 几位数运算，默认是两位
            captcha.setFont(Captcha.FONT_1, 80);
            String str = captcha.getArithmeticString();  // 获取运算的公式：3+2=?
            captcha.text();  // 获取运算的结果：5
            captcha.out(outputStream);
            labelStream.write(String.format("%d.png\t %s\n", i, str).getBytes());
            outputStream.close();
        }

        labelStream.close();

    }
}
