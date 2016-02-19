package com.zy.main;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;
import com.melloware.jintellitype.JIntellitypeException;
import com.zy.main.v3.ScreenShotFrame;

import javax.swing.*;
import java.awt.*;

public class ScreenShot implements HotkeyListener {
    static final int KEY_1 = 88;
    static final int KEY_2 = 89;

    /**
     * 该方法负责监听注册的系统热键事件
     *
     * @param key:触发的热键标识
     */
    public void onHotKey(int key) {
        switch (key) {
            case KEY_1:
                System.out.println("ctrl+alt+I 按下.........");
                frame.startShot();
                break;
            case KEY_2:
                System.out.println("ctrl+alt+O 按下.........");
                System.out.println("截屏程序退出,bye~");
                destroy();
                System.exit(0);
                break;

        }

    }


    /**
     * 解除注册并退出
     */
    void destroy() {
        JIntellitype.getInstance().unregisterHotKey(KEY_1);
        JIntellitype.getInstance().unregisterHotKey(KEY_2);
        System.exit(0);
    }

    /**
     * 初始化热键并注册监听事件
     */
    void initHotkey() {
        //参数KEY_1表示改组热键组合的标识，第二个参数表示组合键，如果没有则为0，该热键对应ctrl+alt+I
        JIntellitype.getInstance().registerHotKey(KEY_1, JIntellitype.MOD_CONTROL + JIntellitype.MOD_ALT,
                (int) 'I');
        JIntellitype.getInstance().registerHotKey(KEY_2, JIntellitype.MOD_CONTROL + JIntellitype.MOD_ALT,
                (int) 'O');
        JIntellitype.getInstance().addHotKeyListener(this);

    }

    private static ScreenShotFrame frame = null;

    public static void main(String[] args) {
        ScreenShot key = new ScreenShot();
        try{

            key.initHotkey();

            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {

                    try {
                        frame = new ScreenShotFrame();
                        frame.startShot();
                    } catch (Exception e) {
                        System.out.println("初始化程序失败！！！！");
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(null, "初始化程序失败："+e.getMessage(), "截屏程序提示", JOptionPane.ERROR_MESSAGE);
                    }

                }
            });
        }catch (JIntellitypeException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Ctrl + Alt + I 或 Ctrl + Alt + O 系统热键被占用", "截屏程序提示", JOptionPane.ERROR_MESSAGE);
        }

    }
}
