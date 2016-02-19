package com.zy.main.v3;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 本程序完成一个基本的屏幕截取任务，用户可以选择截取的范围并保存到文件。
 * <p/>
 * 1.本次修改实现了用户按Esc时退出程序的功能。
 * 2.重新组织了程序，使结构和可读性更好。（起码我认为是这样）
 * 3.使用一个去掉装饰的JFrame替代JWindow来实现主窗体。（主要原因在于，JWindow无法实现键盘事件监听。暂不明原因）
 */
/*public class ScreenShotTest_v3 {

	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {	
			@Override
			public void run() {
		
				try {
					ScreenShotFrame frame=new ScreenShotFrame();
					frame.setVisible(true);
				} catch (AWTException e) {
					System.out.println("初始化程序失败！！！！");
					e.printStackTrace();
				}
				
			}
		});
	}

}*/
/*
 * 主窗口
 */
public class ScreenShotFrame extends JFrame {
    private ShotImagePanel imagePanel = null; //截图面板
    private ActionPanel actionPanel = null;   //操作面板

    public ScreenShotFrame() throws AWTException {

        this.setLayout(null);    //设置布局管理器（这里设置为null是为了便于操作面板定位）
        this.setUndecorated(true);
        this.initComponent();
        this.setFullScreen();

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                //按键事件，当用户按下（Esc）时退出程序
                if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
                    //System.exit(0);
                    ScreenShotFrame.this.stopShot();
                }
            }
        });
    }


    //初始化内部组件
    private void initComponent() throws AWTException {
        //操作面板
        actionPanel = new ActionPanel(this);
        this.add(actionPanel);
        actionPanel.setLocation(0, 0);

        //截图面板
        imagePanel = new ShotImagePanel(this);
        this.add(imagePanel);
    }

    //设置窗口为全屏
    public void setFullScreen() {
        //确保在不支持全屏的环境，也能最大化显示
        this.setSize(imagePanel.getSize());

        //全屏运行
        GraphicsDevice screenDevice = GraphicsEnvironment.
                getLocalGraphicsEnvironment().getDefaultScreenDevice();

        if (screenDevice.isFullScreenSupported()) {
            screenDevice.setFullScreenWindow(this);
        }
    }

    //显示操作
    public void showActions(int x, int y) {

        this.imagePanel.paintShot();

        this.actionPanel.setLocation(x, y);
        this.actionPanel.setVisible(true);
        this.actionPanel.repaint();
    }

    //隐藏操作
    public void hideActions() {
        this.actionPanel.setVisible(false);
    }

    //保存图片
    public void saveImage() {
        //退出全屏状态
        GraphicsDevice screenDevice = GraphicsEnvironment.
                getLocalGraphicsEnvironment().getDefaultScreenDevice();

        if (screenDevice.isFullScreenSupported()) {
            screenDevice.setFullScreenWindow(null);
        }

        try {
            this.imagePanel.saveImage();
        } catch (IOException e) {
            System.out.println("无法完成图片保存操作!");
            e.printStackTrace();
        }
    }

    //设置截取版图片
    public void setClipboardImage(){
        this.imagePanel.setClipboardImage();
    }

    //隐藏截屏程序
    public void stopShot() {
        this.setVisible(false);
    }

    public void startShot() {
        imagePanel.shot();
        this.setVisible(true);
        this.setExtendedState(Frame.MAXIMIZED_BOTH);
        this.requestFocus();
    }
}

//图片面板
class ShotImagePanel extends JPanel {
    private int orgx, orgy, endx, endy;
    private BufferedImage image = null;
    private BufferedImage tempImage = null;
    private BufferedImage saveImage = null;

    private ScreenShotFrame parent = null;

    public void shot() {

        ShotImagePanel.this.parent.hideActions();

        //获取屏幕尺寸
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        this.setBounds(0, 0, d.width, d.height);

        //截取屏幕
        Robot robot = null;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        image = robot.createScreenCapture(new Rectangle(0, 0, d.width, d.height));

        tempImage = null;
        saveImage = null;
    }

    public ShotImagePanel(ScreenShotFrame parent) {

        this.parent = parent;

        this.shot();

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                //记录鼠标开始点击的坐标，并隐藏操作面板
                orgx = e.getX();
                orgy = e.getY();

                ShotImagePanel.this.parent.hideActions();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                //重新定位操作面板，并显示
                ShotImagePanel.this.parent.showActions(e.getX(), e.getY());
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {
                //鼠标拖动时，记录坐标并重绘窗口
                endx = e.getX();
                endy = e.getY();

                paintShot();
            }
        });
    }

    public void paintShot(){
        //临时图像，用于缓冲屏幕区域放置屏幕闪烁
        Image tempImage2 = createImage(ShotImagePanel.this.getWidth(), ShotImagePanel.this.getHeight());
        Graphics g = tempImage2.getGraphics();
        g.drawImage(tempImage, 0, 0, null);

        int x = Math.min(orgx, endx);
        int y = Math.min(orgy, endy);
        int width = Math.abs(endx - orgx) + 1;
        int height = Math.abs(endy - orgy) + 1;
        // 加上1防止width或height0
        g.setColor(Color.BLUE);
        g.drawRect(x - 1, y - 1, width + 1, height + 1);
        //减1加1都了防止图片矩形框覆盖掉
        saveImage = image.getSubimage(x, y, width, height);
        g.drawImage(saveImage, x, y, null);

        ShotImagePanel.this.getGraphics().drawImage(tempImage2, 0, 0, ShotImagePanel.this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        //降低暗度
        RescaleOp ro = new RescaleOp(0.8f, 0, null);
        tempImage = ro.filter(image, null);
        g.drawImage(tempImage, 0, 0, this);
    }

    /***
     * 复制图片到剪切板
     */
    public void setClipboardImage() {
        Transferable trans = new Transferable() {
            @Override
            public Object getTransferData(DataFlavor flavor)
                    throws UnsupportedFlavorException, IOException {
                if (isDataFlavorSupported(flavor)) {
                    return saveImage;
                }
                throw new UnsupportedFlavorException(flavor);
            }

            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[] { DataFlavor.imageFlavor };
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return DataFlavor.imageFlavor.equals(flavor);
            }
        };

        this.getToolkit().getSystemClipboard().setContents(trans, null);
    }

    //保存图像到文件
    public void saveImage() throws IOException {

        JFileChooser jfc = new JFileChooser();
        jfc.setDialogTitle("保存");

        //文件过滤器，用户过滤可选择文件
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG", "jpg");
        jfc.setFileFilter(filter);

        //初始化一个默认文件（此文件会生成到桌面上）
        SimpleDateFormat sdf = new SimpleDateFormat("yyyymmddHHmmss");
        String fileName = sdf.format(new Date());
        File filePath = FileSystemView.getFileSystemView().getHomeDirectory();
        File defaultFile = new File(filePath + File.separator + fileName + ".jpg");
        jfc.setSelectedFile(defaultFile);

        int flag = jfc.showSaveDialog(this);
        if (flag == JFileChooser.APPROVE_OPTION) {
            File file = jfc.getSelectedFile();
            String path = file.getPath();
            //检查文件后缀，放置用户忘记输入后缀或者输入不正确的后缀
            if (!(path.endsWith(".jpg") || path.endsWith(".JPG"))) {
                path += ".jpg";
            }
            //写入文件
            ImageIO.write(saveImage, "jpg", new File(path));
        }

        System.exit(0);
    }
}

//操作面板
class ActionPanel extends JPanel {
    public ScreenShotFrame parent = null;

    //操作面板默认高度和宽度（因为主窗体不是用布局管理器，如果不显示设置组件高度和宽度默认会为零）
    private static final int DEFUATL_WIDTH = 100;
    private static final int DEFAULT_HEIGHT = 33;

    public ActionPanel(ScreenShotFrame parent) {

        this.parent = parent;
        this.initComponent();
        this.setSize(DEFUATL_WIDTH, DEFAULT_HEIGHT);
        this.setVisible(false);
    }

    private void initComponent() {

        this.setLayout(new BorderLayout());

        JToolBar actionBar = new JToolBar("Java 截图");
        actionBar.setFloatable(false);
        //截取版按钮

        JButton yesButton = new JButton();
        ImageIcon icon = new ImageIcon(getImageURL("images/yes.jpg"));
        Image temp = icon.getImage().getScaledInstance(20,
                20, icon.getImage().SCALE_DEFAULT);
        icon = new ImageIcon(temp);
        yesButton.setIcon(icon);

        yesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.stopShot();
                //ActionPanel.this.parent.saveImage();
                ActionPanel.this.parent.setClipboardImage();
            }
        });
        yesButton.setSize(5,5);
        actionBar.add(yesButton);
        //保存按钮
        JButton saveButton = new JButton(new ImageIcon(getImageURL("images/save.gif")));
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.stopShot();
                ActionPanel.this.parent.saveImage();
               // ActionPanel.this.parent.setClipboardImage();
            }
        });
        actionBar.add(saveButton);

        //关闭按钮
        JButton closeButton = new JButton(new ImageIcon(getImageURL("images/close.gif")));
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //System.exit(0);
                parent.stopShot();
            }
        });
        actionBar.add(closeButton);

        this.add(actionBar, BorderLayout.NORTH);
    }

    /**
     * 获取图片的URL
     * @param iconName
     * @return
     */
    private URL getImageURL(String iconName){
        //通过名称直接传入ImageIcon 无法显示  使用classLoader获取URL将从编译的classpath里面查找资源文件 加上"/"表示从根目录查找。
        URL url = getClass().getResource("/"+iconName);
        return url;
    }
}
