package com.zy.main.v2;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.JWindow;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

/*
 * 这是java截图程序的第二个版本，主要解决第一版中不能截取任务栏的问题。这一版中的操作工具栏与第一版不同，这里
 * 是采用面板实现的，而第一版中是采用窗体实现的，之所以如此是因为无法在全屏模式下同时显示两个窗体，因为全屏是独占的。
 * 
 * 此版本存在的问题与不妥之处：
 * 1.程序中的操作栏的宽度和高度是写死的，之所以如此是因为主窗体未使用布局管理器时组件必须指定大小否则为零。
 * 2.本来想为程序添加Esc按键事件，目的是退出程序。但尝试过后未能实现。
 */
public class ScreenShotTest_v2 {

	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {	
			@Override
			public void run() {
				try {
					ScreenShotWindow ssw=new ScreenShotWindow();
					ssw.setAutoRequestFocus(true);
					
					//全屏运行
					GraphicsEnvironment.getLocalGraphicsEnvironment()
					.getDefaultScreenDevice().setFullScreenWindow(ssw);
				} catch (HeadlessException e) {
					e.printStackTrace();
				} catch (AWTException e) {
					e.printStackTrace();
				}
			}
		});
	}

}
/*
 * 主窗口
 */
class ScreenShotWindow extends JWindow
{	
	public ShotImagePanel imagePanel=null; //截图面板
	public ActionPanel actionPanel=null;   //操作面板
	
	public ScreenShotWindow() throws AWTException{
		//设置布局管理器（这里设置为null是为了便于操作面板定位）
		this.setLayout(null);
				
		try{
			//操作面板
			actionPanel=new ActionPanel(this);
			this.add(actionPanel);
			actionPanel.setLocation(0, 0);
			
			//截图面板
			imagePanel=new ShotImagePanel(this);
			this.add(imagePanel);
		}catch(AWTException e) {
			throw new AWTException("无法完成界面初始化！");
		}
	}
}

//图片面板
class ShotImagePanel extends JPanel
{
	private int orgx, orgy, endx, endy;
    private BufferedImage image=null;
    private BufferedImage tempImage=null;
    private BufferedImage saveImage=null;
    
    private ScreenShotWindow parent=null;
    
    public ShotImagePanel(ScreenShotWindow parent) throws AWTException{
    	
    	this.parent=parent;
    	 
		 //获取屏幕尺寸
		 Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		 this.setBounds(0, 0, d.width, d.height);
		 
		 //截取屏幕
		 Robot robot = new Robot();
		 image = robot.createScreenCapture(new Rectangle(0, 0, d.width,d.height));
		 		 
		 this.addMouseListener(new MouseAdapter() {
			 @Override
			public void mousePressed(MouseEvent e) {
				//记录鼠标开始点击的坐标，并隐藏操作面板
	            orgx = e.getX();
	            orgy = e.getY();
	            
	            ShotImagePanel.this.parent.actionPanel.setVisible(false);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				//重新定位操作面板，并显示
				ShotImagePanel.this.parent.actionPanel.setLocation(e.getX(),e.getY());
				ShotImagePanel.this.parent.actionPanel.setVisible(true);
				ShotImagePanel.this.parent.actionPanel.repaint();
			}
		});
		 
		 this.addMouseMotionListener(new MouseMotionAdapter() {
						
			@Override
			public void mouseDragged(MouseEvent e) {
				//鼠标拖动时，记录坐标并重绘窗口
                endx = e.getX();
                endy = e.getY();
                
                //临时图像，用于缓冲屏幕区域放置屏幕闪烁
                Image tempImage2=createImage(ShotImagePanel.this.getWidth(),ShotImagePanel.this.getHeight());
                Graphics g =tempImage2.getGraphics();
                g.drawImage(tempImage, 0, 0, null);
                int x = Math.min(orgx, endx);
                int y = Math.min(orgy, endy);
                int width = Math.abs(endx - orgx)+1;
                int height = Math.abs(endy - orgy)+1;
                // 加上1防止width或height0
                g.setColor(Color.BLUE);
                g.drawRect(x-1, y-1, width+1, height+1);
                //减1加1都了防止图片矩形框覆盖掉
                saveImage = image.getSubimage(x, y, width, height);
                g.drawImage(saveImage, x, y, null);
                
                ShotImagePanel.this.getGraphics().drawImage(tempImage2,0,0,ShotImagePanel.this);
			}
		});
    }
    
    @Override
    public void paint(Graphics g) {
    	super.paint(g);
        RescaleOp ro = new RescaleOp(0.8f, 0, null);
        tempImage = ro.filter(image, null);
        g.drawImage(tempImage, 0, 0, this);
    }
    
    //保存图像到文件
	public void saveImage() throws IOException {
		
		//退出全屏状态
		GraphicsEnvironment.getLocalGraphicsEnvironment()
        .getDefaultScreenDevice().setFullScreenWindow(null);
		
		JFileChooser jfc=new JFileChooser();
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
		if(flag==JFileChooser.APPROVE_OPTION){
			File file=jfc.getSelectedFile();
			String path=file.getPath();
			//检查文件后缀，放置用户忘记输入后缀或者输入不正确的后缀
			if(!(path.endsWith(".jpg")||path.endsWith(".JPG"))){
				path+=".jpg";
			}
			//写入文件
			ImageIO.write(saveImage,"jpg",new File(path));
		}
		
		System.exit(0);
	}
}

//操作面板
class ActionPanel extends JPanel
{
	public ScreenShotWindow parent=null;
	
	//操作面板默认高度和宽度（因为主窗体不是用布局管理器，如果不显示设置组件高度和宽度默认会为零）
	public static final int DEFUATL_WIDTH=63;
	public static final int DEFAULT_HEIGHT=32;
	
	public ActionPanel(ScreenShotWindow parent){
		this.parent=parent;
		this.setVisible(false);
		this.init();
		this.setSize(DEFUATL_WIDTH, DEFAULT_HEIGHT);
	}
	
	private void init(){
		this.setLayout(new BorderLayout());
		JToolBar toolBar=new JToolBar("Java 截图");
		toolBar.setFloatable(false);
		//保存按钮
		JButton saveButton=new JButton(new ImageIcon("images/save.gif"));
		saveButton.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					ActionPanel.this.parent.imagePanel.saveImage();
				} catch (IOException e1) {
					System.out.println("无法完成图片的保存操作！");
					e1.printStackTrace();
				}
			}
		});
		toolBar.add(saveButton);
		
		//关闭按钮
		JButton closeButton=new JButton(new ImageIcon("images/close.gif"));
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		toolBar.add(closeButton);
		
		this.add(toolBar,BorderLayout.NORTH);
	}
}
