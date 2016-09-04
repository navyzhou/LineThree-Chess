package com.yc.games;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import com.swtdesigner.SWTResourceManager;
import com.yc.util.YcUtil;

import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

public class Client implements Runnable{
	protected Shell shell;
	private Text txtLocalhost;
	private Text text;
	private Text text_1;

	private Socket s = null; // 联接服务器的socket
	private DataInputStream dis = null; // 用于从服务器端读数据的流
	private DataOutputStream dos = null; // 用于向服务器写数据的流
	private boolean connected = false; // 标识是否与服务器联接，如果联接了，则为true,没有联接则为false
	private String msg = null; // 用于记录服务器回传信息的字符串
	private Thread t; // 线程对象..
	private Label label_4; //对战信息提示框

	private String uname; //我的昵称
	private String otherName; //对方的昵称
	private boolean isReady = false; //对方已经开战

	private boolean isGo=false; //如果是false为落棋，如果是true为走棋

	private String info=""; //往服务器发的信息
	private Button button_1; //对战按钮

	private boolean flag=true;  //用户是否退出
	private Composite composite;

	private int count=0; //落棋的次数

	private int myscore=9;  //挑战方剩余棋子数
	private int hescore=9;  //迎战方剩余棋子数

	private Label label_6;
	private Label label_8;

	private Label w1; //白子
	private Label w2; //白子
	private Label w3; //白子
	private Label w4; //白子
	private Label w5; //白子
	private Label w6; //白子
	private Label w7; //白子
	private Label w8; //白子
	private Label w9; //白子

	private Label b1; //黑子
	private Label b2; //黑子
	private Label b3; //黑子
	private Label b4; //黑子
	private Label b5; //黑子
	private Label b6; //黑子
	private Label b7; //黑子
	private Label b8; //黑子
	private Label b9; //黑子

	private Label n1;
	private Label n2;
	private Label n3;
	private Label n4;
	private Label n5;
	private Label n6;
	private Label n7;

	private List<Label> lbs=new ArrayList<Label>(); //所有棋子控件
	private int x; //落棋位置
	private int y;

	private List<Label> isNot=new ArrayList<Label>();  //所有不能落子的控件

	private int numx; //落棋偏移量
	private int numy;

	private int x1;
	private int y1;

	private boolean isRight=false;  //是否已经成三

	private Label isChecked=null; //走棋时是否已经选择了棋
	private boolean gameOver=false;  //1：我赢   -1：他赢
	private boolean again=false; //再来一局

	//棋盘信息  -1不可落棋  0可以落棋  1挑战方棋子  2迎战方棋子  3被吃棋子
	private String[][] point=new String[][]{
			{"0","-1","-1","0","-1","-1","0"},
			{"-1","0","-1","0","-1","0","-1"},
			{"-1","-1","0","0","0","-1","-1"},
			{"0","0","0","-1","0","0","0"},
			{"-1","-1","0","0","0","-1","-1"},
			{"-1","0","-1","0","-1","0","-1"},
			{"0","-1","-1","0","-1","-1","0"},
	};

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Client window = new Client();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell(SWT.MIN);
		shell.setImage(SWTResourceManager.getImage(Client.class, "/images/qi.png"));
		shell.setSize(850, 669);
		shell.setLocation(Display.getCurrent().getClientArea().width/2-shell.getShell().getSize().x/2, Display.getCurrent().getClientArea().height/2 - shell.getSize().y /2);
		shell.setText("成三棋客户端");

		Group group = new Group(shell, SWT.NONE);
		group.setBounds(10, 10, 736, 53);

		Label label = new Label(group, SWT.NONE);
		label.setBounds(10, 21, 72, 15);
		label.setText("服务器地址：");

		txtLocalhost = new Text(group, SWT.BORDER);
		txtLocalhost.setText("localhost");
		txtLocalhost.setBounds(88, 18, 131, 21);

		Label label_1 = new Label(group, SWT.NONE);
		label_1.setBounds(238, 21, 36, 15);
		label_1.setText("昵称：");

		text = new Text(group, SWT.BORDER);
		text.setBounds(280, 18, 99, 21);

		Label label_2 = new Label(group, SWT.NONE);
		label_2.setBounds(395, 21, 61, 15);
		label_2.setText("对战好友：");

		text_1 = new Text(group, SWT.BORDER);
		text_1.setBounds(459, 18, 93, 21);

		button_1 = new Button(group, SWT.NONE);
		button_1.setBounds(576, 16, 72, 25);
		button_1.setText("对战");

		Button button_2 = new Button(group, SWT.NONE);
		button_2.setBounds(654, 16, 72, 25);
		button_2.setText("退出");

		Label label_3 = new Label(shell, SWT.NONE);
		label_3.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
		label_3.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		label_3.setBounds(33, 69, 80, 20);
		label_3.setText("对战信息：");

		label_4 = new Label(shell, SWT.NONE);
		label_4.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		label_4.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
		label_4.setBounds(119, 69, 601, 20);

		composite = new Composite(shell, SWT.NONE);
		composite.setBackgroundMode(SWT.INHERIT_DEFAULT);
		composite.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if(gameOver){//游戏结束
					init();
				}else{
					if(isGo&&isChecked!=null){ //如果已经开始走起并且用户已经选中了棋子
						//那么判断是否可以将选择的棋子走到这里
						if(e.x<160||e.x>660||e.y>500){
							//YcUtil.showMsg(shell,"提示",e.x+"\t"+e.y);
							return;
						}else{
							if(isGoHere(e)){
								label_4.setText("请等待对方落子...");
								isGo=false;
								isChecked=null;
							}else{
								label_4.setText("对不起,不能这样走棋...");
							}
						}
					}

					if(!again){
						if(!isRight){ //说明已经成三
							if(isReady){//说明已经开战
								if(count==0){
									again=false;
								}
								if(e.x<160||e.x>660||e.y>500){
									return;
								}
								count++;
								if(count<=9){
									switch(count){
									case 1:setPoint(w1,e,1);break;
									case 2:setPoint(w2,e,2);break;
									case 3:setPoint(w3,e,3);break;
									case 4:setPoint(w4,e,4);break;
									case 5:setPoint(w5,e,5);break;
									case 6:setPoint(w6,e,6);break;
									case 7:setPoint(w7,e,7);break;
									case 8:setPoint(w8,e,8);break;
									case 9:setPoint(w9,e,9);break;
									}
								}else{
									isGo=true; //开始走棋
									n1.dispose();//销毁不能落棋的标记
									n2.dispose();//销毁不能落棋的标记
									n3.dispose();//销毁不能落棋的标记
									n4.dispose();//销毁不能落棋的标记
									n5.dispose();//销毁不能落棋的标记
									n6.dispose();//销毁不能落棋的标记
									n7.dispose();//销毁不能落棋的标记
									label_4.setText("开始走棋...");
									isReady=false;
								}
							}else{
								if(count>0){
									label_4.setText("请等待对方落子...");
								}else{
									label_4.setText("对方还没有上线,请等待...");
								}
							}
						}else{
							label_4.setText("您已成三,请吃对方一颗棋子...");
						}
					}
				}
			}
		});
		composite.setBackgroundImage(SWTResourceManager.getImage(Client.class, "/images/qip.png"));
		composite.setBounds(0, 106, 844, 524);

		Label label_5 = new Label(composite, SWT.NONE);
		label_5.setLocation(20, 10);
		label_5.setSize(91, 15);
		label_5.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 10, SWT.NORMAL));
		label_5.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
		label_5.setText("我方剩余棋子：");

		label_6 = new Label(composite, SWT.NONE);
		label_6.setLocation(117, 10);
		label_6.setSize(27, 15);
		label_6.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 10, SWT.NORMAL));
		label_6.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
		label_6.setText("9");

		Label label_9 = new Label(composite, SWT.NONE);
		label_9.setLocation(38, 35);
		label_9.setSize(80, 15);
		label_9.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 10, SWT.NORMAL));
		label_9.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
		label_9.setText("我方待落棋子");

		label_8 = new Label(composite, SWT.NONE);
		label_8.setLocation(807, 8);
		label_8.setSize(27, 15);
		label_8.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 11, SWT.NORMAL));
		label_8.setForeground(SWTResourceManager.getColor(SWT.COLOR_MAGENTA));
		label_8.setText("9");

		Label label_10 = new Label(composite, SWT.NONE);
		label_10.setLocation(720, 35);
		label_10.setSize(80, 15);
		label_10.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 10, SWT.NORMAL));
		label_10.setForeground(SWTResourceManager.getColor(SWT.COLOR_MAGENTA));
		label_10.setText("对方待落棋子");



		Label label_7 = new Label(composite, SWT.NONE);
		label_7.setBounds(710, 10, 91, 15);
		label_7.setForeground(SWTResourceManager.getColor(SWT.COLOR_MAGENTA));
		label_7.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 10, SWT.NORMAL));
		label_7.setText("对方剩余棋子：");

		init();

		//点击对战
		button_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(count!=0){
					init();
				}
				try {
					uname = text.getText().toString().trim();
					otherName = text_1.getText().toString().trim();

					if (YcUtil.isNull(uname)) {
						YcUtil.showMsg(shell, "错误提示", "请输入您的昵称...");
						text.setFocus();
						return;
					}

					if (YcUtil.isNull(otherName)) {
						YcUtil.showMsg(shell, "错误提示", "请输入您的对战好友昵称...");
						text_1.setFocus();
						return;
					}

					if (uname.equals(otherName)) {
						YcUtil.showMsg(shell, "错误提示", "自己不能跟自己对战...");
						text_1.setFocus();
						return;
					}

					boolean bl=connect();
					// 启动临听线程, 会调用 run()方法
					if(bl){ //如果连接服务器成功
						text.setEnabled(false);
						text_1.setEnabled(false);
						button_1.setEnabled(false);
						t = new Thread(Client.this); // Client.this表示当前的客户端对象，这样这个对象的run()方法就会被启动
						t.start(); // run方法中写入从服务器中读取服务器信息的操作.

						info = "userInfo." + uname + "-" + otherName;
						dos.writeUTF(info);
						dos.flush();
						flag=true;//对方已经上线
					}else{
						YcUtil.showMsg(shell, "失败提示", "连接服务器失败");
					}
				} catch (Exception e1) {
					YcUtil.showMsg(shell, "发送信息失败", "发送信息失败");
				}
			}
		});

		//退出对战
		button_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (dos != null) {
						dos.writeUTF("end." + text.getText().toString().trim());
						dos.flush();
					}
					flag = false;  //用户退出，结束线程
					System.exit(0);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});


	}

	public void init(){
		isReady = true; //对方已经开战
		isGo=false; //如果是false为落棋，如果是true为走棋
		info=""; //往服务器发的信息
		flag=true;  //用户是否退出
		count=0; //落棋的次数

		myscore=9;  //挑战方剩余棋子数
		hescore=9;  //迎战方剩余棋子数
		isNot=new ArrayList<Label>();  //所有不能落子的控件

		numx=0; //落棋偏移量
		numy=0;

		x1=0;
		y1=0;
		isRight=false;  //是否已经成三
		isChecked=null; //走棋时是否已经选择了棋
		gameOver=false;  //1：我赢   -1：他赢

		again=false;

		//棋盘信息  -1不可落棋  0可以落棋  1挑战方棋子  2迎战方棋子  3被吃棋子
		point=new String[][]{
				{"0","-1","-1","0","-1","-1","0"},
				{"-1","0","-1","0","-1","0","-1"},
				{"-1","-1","0","0","0","-1","-1"},
				{"0","0","0","-1","0","0","0"},
				{"-1","-1","0","0","0","-1","-1"},
				{"-1","0","-1","0","-1","0","-1"},
				{"0","-1","-1","0","-1","-1","0"},
		};
		
		lbs=new ArrayList<Label>(); //所有棋子控件
		if(w1==null||w1.isDisposed()){
			w1 = new Label(composite,SWT.NONE);
		}
		w1.setToolTipText("w1");
		w1.setLocation(59, 58);
		w1.setSize(44, 44);
		w1.setImage(SWTResourceManager.getImage(Client.class, "/images/1.png"));

		if(w2==null||w2.isDisposed()){
			w2 = new Label(composite, SWT.NONE);
		}
		w2.setToolTipText("w2");
		w2.setLocation(59, 108);
		w2.setSize(44, 44);
		w2.setImage(SWTResourceManager.getImage(Client.class, "/images/1.png"));

		if(w3==null||w3.isDisposed()){
			w3 = new Label(composite, SWT.NONE);
		}
		w3.setToolTipText("w3");
		w3.setLocation(59, 158);
		w3.setSize(44, 44);
		w3.setImage(SWTResourceManager.getImage(Client.class, "/images/1.png"));

		if(w4==null||w4.isDisposed()){
			w4 = new Label(composite, SWT.NONE);
		}
		w4.setToolTipText("w4");
		w4.setLocation(59, 208);
		w4.setSize(44, 44);
		w4.setImage(SWTResourceManager.getImage(Client.class, "/images/1.png"));

		if(w5==null||w5.isDisposed()){
			w5 = new Label(composite, SWT.NONE);
		}
		w5.setToolTipText("w5");
		w5.setLocation(59, 258);
		w5.setSize(44, 44);
		w5.setImage(SWTResourceManager.getImage(Client.class, "/images/1.png"));

		if(w6==null||w6.isDisposed()){
			w6 = new Label(composite, SWT.NONE);
		}
		w6.setToolTipText("w6");
		w6.setLocation(59, 305);
		w6.setSize(44, 44);
		w6.setImage(SWTResourceManager.getImage(Client.class, "/images/1.png"));

		if(w7==null||w7.isDisposed()){
			w7 = new Label(composite, SWT.NONE);
		}
		w7.setToolTipText("w7");
		w7.setLocation(59, 352);
		w7.setSize(44, 44);
		w7.setImage(SWTResourceManager.getImage(Client.class, "/images/1.png"));

		if(w8==null||w8.isDisposed()){
			w8 = new Label(composite, SWT.NONE);
		}
		w8.setToolTipText("w8");
		w8.setLocation(59, 403);
		w8.setSize(44, 44);
		w8.setImage(SWTResourceManager.getImage(Client.class, "/images/1.png"));

		if(w9==null||w9.isDisposed()){
			w9 = new Label(composite, SWT.NONE);
		}
		w9.setToolTipText("w9");
		w9.setLocation(59, 453);
		w9.setSize(44, 44);
		w9.setImage(SWTResourceManager.getImage(Client.class, "/images/1.png"));

		if(b1==null||b1.isDisposed()){
			b1 = new Label(composite, SWT.NONE);
		}
		b1.setToolTipText("b1");
		b1.setLocation(739, 60);
		b1.setSize(44, 44);
		b1.setImage(SWTResourceManager.getImage(Client.class, "/images/2.png"));

		if(b2==null||b2.isDisposed()){
			b2 = new Label(composite, SWT.NONE);
		}
		b2.setToolTipText("b2");
		b2.setLocation(739, 110);
		b2.setSize(44, 44);
		b2.setImage(SWTResourceManager.getImage(Client.class, "/images/2.png"));

		if(b3==null||b3.isDisposed()){
			b3 = new Label(composite, SWT.NONE);
		}
		b3.setToolTipText("b3");
		b3.setLocation(739, 160);
		b3.setSize(44, 44);
		b3.setImage(SWTResourceManager.getImage(Client.class, "/images/2.png"));

		if(b4==null||b4.isDisposed()){
			b4 = new Label(composite, SWT.NONE);
		}
		b4.setToolTipText("b4");
		b4.setLocation(739, 210);
		b4.setSize(44, 44);
		b4.setImage(SWTResourceManager.getImage(Client.class, "/images/2.png"));

		if(b5==null||b5.isDisposed()){
			b5 = new Label(composite, SWT.NONE);
		}
		b5.setToolTipText("b5");
		b5.setLocation(739, 260);
		b5.setSize(44, 44);
		b5.setImage(SWTResourceManager.getImage(Client.class, "/images/2.png"));

		if(b6==null||b6.isDisposed()){
			b6 = new Label(composite, SWT.NONE);
		}
		b6.setToolTipText("b6");
		b6.setLocation(739, 307);
		b6.setSize(44, 44);
		b6.setImage(SWTResourceManager.getImage(Client.class, "/images/2.png"));

		if(b7==null||b7.isDisposed()){
			b7 = new Label(composite, SWT.NONE);
		}
		b7.setToolTipText("b7");
		b7.setLocation(739, 354);
		b7.setSize(44, 44);
		b7.setImage(SWTResourceManager.getImage(Client.class, "/images/2.png"));

		if(b8==null||b8.isDisposed()){
			b8 = new Label(composite, SWT.NONE);
		}
		b8.setToolTipText("b8");
		b8.setLocation(739, 405);
		b8.setSize(44, 44);
		b8.setImage(SWTResourceManager.getImage(Client.class, "/images/2.png"));

		if(b9==null||b9.isDisposed()){
			b9 = new Label(composite, SWT.NONE);
		}
		b9.setToolTipText("b9");
		b9.setLocation(739, 455);
		b9.setSize(44, 44);
		b9.setImage(SWTResourceManager.getImage(Client.class, "/images/2.png"));

		lbs.add(w1);
		lbs.add(w2);
		lbs.add(w3);
		lbs.add(w4);
		lbs.add(w5);
		lbs.add(w6);
		lbs.add(w7);
		lbs.add(w8);
		lbs.add(w9);

		n1 = new Label(composite, SWT.NONE);
		n1.setVisible(false);
		n1.setImage(SWTResourceManager.getImage(Client.class, "/images/n1.png"));
		n1.setBounds(10, 476, 42, 42);

		n2 = new Label(composite, SWT.NONE);
		n2.setVisible(false);
		n2.setBounds(11, 476, 42, 42);
		n2.setImage(SWTResourceManager.getImage(Client.class, "/images/n1.png"));

		n3 = new Label(composite, SWT.NONE);
		n3.setVisible(false);
		n3.setBounds(10, 476, 42, 42);
		n3.setImage(SWTResourceManager.getImage(Client.class, "/images/n1.png"));

		n4 = new Label(composite, SWT.NONE);
		n4.setVisible(false);
		n4.setBounds(10, 476, 42, 42);
		n4.setImage(SWTResourceManager.getImage(Client.class, "/images/n1.png"));

		n5 = new Label(composite, SWT.NONE);
		n5.setVisible(false);
		n5.setBounds(10, 476, 42, 42);
		n5.setImage(SWTResourceManager.getImage(Client.class, "/images/n1.png"));

		n6 = new Label(composite, SWT.NONE);
		n6.setVisible(false);
		n6.setBounds(10, 476, 42, 42);
		n6.setImage(SWTResourceManager.getImage(Client.class, "/images/n1.png"));

		n7 = new Label(composite, SWT.NONE);
		n7.setVisible(false);
		n7.setBounds(10, 476, 42, 42);
		n7.setImage(SWTResourceManager.getImage(Client.class, "/images/n1.png"));

		isNot.add(n1);
		isNot.add(n2);
		isNot.add(n3);
		isNot.add(n4);
		isNot.add(n5);
		isNot.add(n6);
		isNot.add(n7);

		label_6.setText("9");
		label_8.setText("9");
	}

	/**
	 * 用于建立与服务器接接的方法
	 */
	public boolean connect() {
		try {
			s = new Socket(txtLocalhost.getText().toString().trim(),8899); // 建立联接
			dis = new DataInputStream(s.getInputStream());
			dos = new DataOutputStream(s.getOutputStream());
			connected = true; // 改变标识变量状态
			label_4.setText("服务器连接成功,等待好友对战好友上线...");
			return true;
		} catch (Exception e) {
			YcUtil.showMsg(shell, "出错啦","连接服务器失败,服务器可能没有启动哦....");
			connected = false; // 改变标识变量状态
			return false;
		}
	}

	/**
	 * 退出的方法 关闭联接 socket , connected=false.
	 */
	public void disConnect() {
		if (s != null) {
			try {
				connected = false;
				s.close();
			} catch (IOException e) {
				YcUtil.showMsg(shell, "关闭联接错误", e.getMessage());
			}
		}
	}

	/**
	 * 客户机向服务器发送信息
	 */
	public void send(String info){
		try {
			dos.writeUTF(info);
			dos.flush();
		} catch (IOException e) { //如果客户机掉线
			YcUtil.showMsg(shell, "信息发送失败","客服端与服务器已断开连接...");
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		while (flag) {
			try {
				if (connected == true && s.isConnected() == true&& s.isClosed() == false) {
					// 从服务器读信息
					msg = dis.readUTF();
					if (msg.startsWith("ready.")) { // 用户信息
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								label_4.setText("对方已上线,对战开始,请落子...");
								isReady=true;
							}
						});
					}else if (msg.startsWith("error.")) { // 错误提示
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								text.setEnabled(true);
								text_1.setEnabled(true);
								button_1.setEnabled(true);
								label_4.setText("该昵称已被使用或该对战好友已经在对战,请重新输入...");
							}
						});
						connected = false; // 断开连接
					}else if(msg.startsWith("wait.")){
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								label_4.setText("对战开始,等待对方落子...");
							}
						});
					}else if (msg.startsWith("dataInfo.")) { //dataInfo.x~y|1
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								String x=msg.substring(msg.indexOf(".")+1,msg.indexOf("~"));  //x
								String y=msg.substring(msg.indexOf("~")+1,msg.indexOf("|"));  //y
								String mk=msg.substring(msg.indexOf("|")+1);  //i

								switch(Integer.parseInt(mk)){
								case 1:setPointOther(b1,Integer.parseInt(x),Integer.parseInt(y));break;
								case 2:setPointOther(b2,Integer.parseInt(x),Integer.parseInt(y));break;
								case 3:setPointOther(b3,Integer.parseInt(x),Integer.parseInt(y));break;
								case 4:setPointOther(b4,Integer.parseInt(x),Integer.parseInt(y));break;
								case 5:setPointOther(b5,Integer.parseInt(x),Integer.parseInt(y));break;
								case 6:setPointOther(b6,Integer.parseInt(x),Integer.parseInt(y));break;
								case 7:setPointOther(b7,Integer.parseInt(x),Integer.parseInt(y));break;
								case 8:setPointOther(b8,Integer.parseInt(x),Integer.parseInt(y));break;
								case 9:setPointOther(b9,Integer.parseInt(x),Integer.parseInt(y));break;
								}
							}
						});
					}else if (msg.startsWith("lose.")) { //dataInfo.x~y|1
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								String x=msg.substring(msg.indexOf(".")+1,msg.indexOf("~"));  //x
								String y=msg.substring(msg.indexOf("~")+1,msg.indexOf("|"));  //y
								String mk=msg.substring(msg.indexOf("|")+1);  //i

								switch(Integer.parseInt(mk)){
								case 1:isLose(w1,Integer.parseInt(x),Integer.parseInt(y));break;
								case 2:isLose(w2,Integer.parseInt(x),Integer.parseInt(y));break;
								case 3:isLose(w3,Integer.parseInt(x),Integer.parseInt(y));break;
								case 4:isLose(w4,Integer.parseInt(x),Integer.parseInt(y));break;
								case 5:isLose(w5,Integer.parseInt(x),Integer.parseInt(y));break;
								case 6:isLose(w6,Integer.parseInt(x),Integer.parseInt(y));break;
								case 7:isLose(w7,Integer.parseInt(x),Integer.parseInt(y));break;
								case 8:isLose(w8,Integer.parseInt(x),Integer.parseInt(y));break;
								case 9:isLose(w9,Integer.parseInt(x),Integer.parseInt(y));break;
								}
								Display.getDefault().asyncExec(new Runnable() {
									@Override
									public void run() {
										isReady=true; //不能再下棋
										label_4.setText("对方已吃子,请落子...");
									}
								});
							}
						});
					}else if (msg.startsWith("move.")) { //对方移子dataInfo.x~y|1
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								//this.send("move."+uname+"-"+px2+"~"+py2+"|"+isChecked.getToolTipText().replace("w",""));
								String x=msg.substring(msg.indexOf(".")+1,msg.indexOf("~"));  //x
								String y=msg.substring(msg.indexOf("~")+1,msg.indexOf("|"));  //y
								String mk=msg.substring(msg.indexOf("|")+1);  //i

								if(n1!=null){
									n1.dispose();//销毁不能落棋的标记
									n2.dispose();//销毁不能落棋的标记
									n3.dispose();//销毁不能落棋的标记
									n4.dispose();//销毁不能落棋的标记
									n5.dispose();//销毁不能落棋的标记
									n6.dispose();//销毁不能落棋的标记
									n7.dispose();//销毁不能落棋的标记
									count=10;
								}

								switch(Integer.parseInt(mk)){
								case 1:isMove(b1,Integer.parseInt(x),Integer.parseInt(y));break;
								case 2:isMove(b2,Integer.parseInt(x),Integer.parseInt(y));break;
								case 3:isMove(b3,Integer.parseInt(x),Integer.parseInt(y));break;
								case 4:isMove(b4,Integer.parseInt(x),Integer.parseInt(y));break;
								case 5:isMove(b5,Integer.parseInt(x),Integer.parseInt(y));break;
								case 6:isMove(b6,Integer.parseInt(x),Integer.parseInt(y));break;
								case 7:isMove(b7,Integer.parseInt(x),Integer.parseInt(y));break;
								case 8:isMove(b8,Integer.parseInt(x),Integer.parseInt(y));break;
								case 9:isMove(b9,Integer.parseInt(x),Integer.parseInt(y));break;
								}
							}
						});
					}else if (msg.startsWith("end.")) {
						disConnect(); //关闭连接
						if(t!=null){
							t.stop();
						}
						System.exit(0); 
					}else if (msg.startsWith("gameover.")) { //游戏结束
						String endresult=msg.substring(msg.indexOf("-")+1);
						if("1".equals(endresult)){
							Display.getDefault().asyncExec(new Runnable() {
								@Override
								public void run() {
									isReady=true; //不能再下棋
									label_4.setText("恭喜您,您赢了...");
									gameOver=true;
									YcUtil.showMsg(shell,"胜利提示","恭喜您,您赢了.点击棋盘继续开战....");
								}
							});
						}else{
							Display.getDefault().asyncExec(new Runnable() {
								@Override
								public void run() {
									isReady=true; //不能再下棋
									gameOver=true;
									label_4.setText("很遗憾,您输了...");
									YcUtil.showMsg(shell,"失败提示","很遗憾,您输了.点击棋盘再来一盘....");
								}
							});
						}
					} else if (msg.startsWith("otherend.")) {
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								text.setEnabled(true);
								text_1.setEnabled(true);
								button_1.setEnabled(true);
								isReady=false; //不能再下棋
								label_4.setText("对方已下线...");
							}
						});
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				YcUtil.showMsg(shell, "失败信息","服务器连接失败...");
			}
		}
	}


	//落子的方法
	public void setPoint(final Label lb,MouseEvent e,int i){
		x=e.x/76;
		y=e.y/76;

		y1=x<=2?0:x-2;
		x1=y;

		//判断是否可以落子
		if("0".equals(point[x1][y1])){
			point[x1][y1]="1";
			numx=4;
			numy=0;

			//修正坐标
			if(y>=3){
				numy+=8;
			}

			if(x>2&&x<=5){
				numx+=4;
			}

			if(x>5){
				numx+=12;
			}
			lb.setBounds(x*76+numx,y*76+numy,44,44);
			this.send("data."+uname+"-"+x+"~"+y+"|"+i);
			boolean rs=isSan(x1,y1,"1");
			if(rs){
				isReady=false;
				isRight=true;
				label_4.setText("您已经成三,请吃子...");
			}else{
				isReady=false;
				label_4.setText("您已落子,等待对方落子...");
			}

			lb.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseUp(MouseEvent e) {
					if(isGo){
						isChecked=lb;
						for(Label ls:lbs){
							ls.setImage(SWTResourceManager.getImage(Client.class, "/images/1.png"));
						}
						lb.setImage(SWTResourceManager.getImage(Client.class, "/images/1s.png"));
					}else{
						YcUtil.showMsg(shell, "温馨提示","对不起,现在还是落棋阶段...");
					}
				}
			});
		}else{
			YcUtil.showMsg(shell,"错误提示","此处不能落子");
			count--;
		}
	}

	//对方落子
	public void setPointOther(final Label lb,final int x,final int y){
		y1=x<=2?0:x-2;
		x1=y;

		//判断是否可以落子
		point[x1][y1]="2";
		numx=4;
		numy=0;

		//修正坐标
		if(y>=3){
			numy+=8;
		}

		if(x>2&&x<=5){
			numx+=4;
		}

		if(x>5){
			numx+=12;
		}
		lb.setBounds(x*76+numx,y*76+numy,44,44);
		boolean rs=isSan(x1,y1,"2");
		if(rs){
			isReady=false;
			label_4.setText("对方已经成三,等待对方吃子...");
		}else{
			isReady=true;
			label_4.setText("对方已落子,请下子...");
		}

		//给对方的棋子绑定一个方法，用于吃子
		lb.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if(isRight){//如果我已经成三
					if(isGiveMe(lb)){ //如果可以被吃
						hescore--;
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								label_8.setText(hescore+"");
							}
						});
						if(hescore<=2){
							gameOver=true; //我赢
							Display.getDefault().asyncExec(new Runnable() {
								@Override
								public void run() {
									label_4.setText("恭喜您,您赢了....");
								}
							});
							send("gameover."+uname+"-0");
							YcUtil.showMsg(shell,"成功提示","恭喜您,您赢了.点击棋盘继续开战....");
						}
						lb.dispose();
						isRight=false; //修改成三的标志
						label_4.setText("等待对方下子...");
					}else{
						label_4.setText("该子已经成三,您不能吃,请换个子吧...");
					}
				}else{
					label_4.setText("您还未成三,请继续落棋或走棋...");
				}
			}
		});
	}

	/**
	 * 判断是否可以走棋
	 */
	public boolean isGoHere(MouseEvent e){
		int px1;
		int py1;

		int x1=0;
		int y1=0;

		int px2=e.x/76;
		int py2=e.y/76;

		int y2=px2<=2?0:px2-2;
		int x2=py2;

		if(isChecked!=null){
			px1=isChecked.getLocation().x/76; //获取当前Label的x轴
			py1=isChecked.getLocation().y/76; //获取当前Label的y轴

			y1=px1<=2?0:px1-2;
			x1=py1;

			numx=4;
			numy=0;

			//修正坐标
			if(y2>=3){
				numy+=8;
			}

			if(x2>2&&x2<=5){
				numx+=4;
			}

			if(x2>5){
				numx+=12;
			}

			//YcUtil.showMsg(shell,"提示",x1+"\t"+y1+"\t"+point[x1][y1]+"\t"+x2+"\t"+y2+"\t"+point[x2][y2]);
			if("-1".equals(point[x2][y2])){
				return false;
			}else if(x1!=x2&&y1!=y2&&(x1!=x2-1&&y1!=y2-1&&x1!=3)&&(x1!=x2+1&&y1!=y2+1&&x1!=3)){//如果两个点不在同一直线
				return false;
			}else if((x1==3&&y==1)||(x1==3&&y==5)&&Math.abs(y1-y2)>2){
				return false;
			}else{
				if(!"0".equals(point[x2][y2])&&!"4".equals(point[x2][y2])&&!"-1".equals(point[x2][y2])){
					return false;
				}else{
					if(x1==x2){
						if(y1>y2){
							for(int i=y2+1;i<y1;i++){
								if(!"0".equals(point[x1][i])&&!"4".equals(point[x1][i])&&!"-1".equals(point[x1][i])){
									return false;
								}
							}
						}else{
							for(int i=y1+1;i<y2;i++){
								if(!"0".equals(point[x1][i])&&!"4".equals(point[x1][i])&&!"-1".equals(point[x1][i])){
									return false;
								}
							}
						}
					}else if(y1==y2){
						if(x1>x2){
							for(int i=x2+1;i<x1;i++){
								if(!"0".equals(point[i][y1])&&!"4".equals(point[i][y1])&&!"-1".equals(point[i][y1])){
									return false;
								}
							}
						}else{
							for(int i=x1+1;i<x2;i++){
								if(!"0".equals(point[i][y1])&&!"4".equals(point[i][y1])&&!"-1".equals(point[i][y1])){
									return false;
								}
							}
						}
					}else{
						if(x1==3||y1==3||x2==3||y2==3||Math.abs(x1-x2)>1||Math.abs(y1-y2)>1){
							return false;
						}
					}
					point[x1][y1]="0";
					point[x2][y2]="1";
					isChecked.setLocation(px2*76+numx,py2*76+numy);
					isChecked.setImage(SWTResourceManager.getImage(Client.class, "/images/1.png"));

					//YcUtil.showMsg(shell,"提示","来咯....");

					this.send("move."+uname+"-"+px2+"~"+py2+"|"+isChecked.getToolTipText().replace("w",""));

					if(isSan(x2,y2,"1")){
						isRight=true;
					}else{
						isRight=false;
					}
					return true;
				}
			}
		}else{
			return false;
		}
	}


	/**
	 * 判断是否已经成三
	 * @return
	 */
	public boolean isSan(int x,int y,String f){
		boolean s=false;  //判断是否成三
		boolean tp=false;

		//如果是三个正方形的端点  12个点
		if((x==0&&y==0)||(x==0&&y==6)||(x==6&&y==0)||(x==6&&y==6)||
				(x==1&&y==1)||(x==1&&y==5)||(x==5&&y==1)||(x==5&&y==5)||
				(x==2&&y==2)||(x==2&&y==4)||(x==4&&y==2)||(x==4&&y==4)){
			//横向
			for(int i=0;i<7;i++){
				//如果值不等于1|2或-1,说明中间有空位或者是他的棋子
				if(!f.equals(point[x][i])&&!"-1".equals(point[x][i])){
					tp=true;  //有不符合规范的
					break;
				}
			}
			if(!tp){
				s=true;
			}

			//纵向
			if(!s){
				tp=false;
				for(int i=0;i<7;i++){
					//如果值不等于1|2或-1,说明中间有空位或者是他的棋子
					if(!f.equals(point[i][y])&&!"-1".equals(point[i][y])){
						tp=true;  //有不符合规范的
						break;
					}
				}
			}
			if(!tp){
				s=true;
			}
		}

		//如果是最外层或最里层正方形的横向中间点 4个点
		if((x==0&&y==3)||(x==2&&y==3)||(x==6&&y==3)||(x==4&&y==3)){
			tp=false;
			//横向
			for(int i=0;i<7;i++){
				//如果值不等于1|2或-1,说明中间有空位或者是他的棋子
				if(!f.equals(point[x][i])&&!"-1".equals(point[x][i])){
					tp=true;
					break;
				}
			}
			if(!tp){
				s=true;
			}

			//纵向
			if(!s){
				tp=false;
				if(x==0||x==4){
					for(int i=x;i<x+3;i++){
						//如果值不等于1|2或-1,说明中间有空位或者是他的棋子
						if(!f.equals(point[i][y])&&!"-1".equals(point[i][y])){
							tp=true;
							break;
						}
					}
					if(!tp){
						s=true;
					}
				}else{
					for(int i=x;i>x-3;i--){
						//如果值不等于1|2或-1,说明中间有空位或者是他的棋子
						if(!f.equals(point[i][y])&&!"-1".equals(point[i][y])){
							tp=true;
							break;
						}
					}
					if(!tp){
						s=true;
					}
				}
			}
		}

		//如果是最外层或最里层正方形的纵向中间点 4个点
		if((x==3&&y==0)||(x==3&&y==2)||(x==3&&y==4)|(x==3&&y==6)){
			tp=false;
			//横向
			if(y==0||y==4){
				for(int i=y;i<y+3;i++){
					//如果值不等于1|2或-1,说明中间有空位或者是他的棋子
					if(!f.equals(point[x][i])&&!"-1".equals(point[x][i])){
						tp=true;
						break;
					}
				}
				if(!tp){
					s=true;
				}
			}else{
				for(int i=y;i>y-3;i--){
					//如果值不等于1|2或-1,说明中间有空位或者是他的棋子
					if(!f.equals(point[x][i])&&!"-1".equals(point[x][i])){
						tp=true;
						break;
					}
				}
				if(!tp){
					s=true;
				}
			}


			//纵向
			if(!s){
				tp=false;
				for(int i=0;i<7;i++){
					//如果值不等于1|2或-1,说明中间有空位或者是他的棋子
					if(!f.equals(point[i][y])&&!"-1".equals(point[i][y])){
						tp=true;
						break;
					}
				}
				if(!tp){
					s=true;
				}
			}
		}

		//两个点
		if((x==1&&y==3)||(x==5&&y==3)){
			tp=false;

			//横向
			for(int i=1;i<6;i++){
				//如果值不等于1|2或-1,说明中间有空位或者是他的棋子
				if(!f.equals(point[x][i])&&!"-1".equals(point[x][i])){
					tp=true;
					break;
				}
			}
			if(!tp){
				s=true;
			}

			//纵向
			if(!s){
				tp=false;
				for(int i=x-1;i<=x+1;i++){
					if(!f.equals(point[i][y])&&!"-1".equals(point[i][y])){
						tp=true;
						break;
					}
				}
				if(!tp){
					s=true;
				}
			}
		}

		//两个点
		if((x==3&&y==1)||(x==3&&y==5)){
			tp=false;

			//横向
			for(int i=y-1;i<=y+1;i++){
				//如果值不等于1|2或-1,说明中间有空位或者是他的棋子
				if(!f.equals(point[x][i])&&!"-1".equals(point[x][i])){
					tp=true;
					break;
				}
			}
			if(!tp){
				s=true;
			}

			//纵向
			if(!s){
				tp=false;
				for(int i=x-2;i<=x+2;i++){
					if(!f.equals(point[i][y])&&!"-1".equals(point[i][y])){
						tp=true;
						break;
					}
				}
				if(!tp){
					s=true;
				}
			}
		}

		return s;
	}

	/**
	 * 判断是否可以吃
	 * @return
	 */
	public boolean isGiveMe(Label l){
		int px=l.getLocation().x/76; //获取当前Label的x轴
		int py=l.getLocation().y/76; //获取当前Label的y轴

		int lx=0;
		int ly=0;

		ly=px<=2?0:px-2;
		lx=py;

		//YcUtil.showMsg(shell, "提示",lx+"\t"+ly+point[lx][ly]);

		if(isSan(lx,ly,"2")){
			return false;
		}else{
			if(count<=9){//如果是落棋阶段
				isNot.get(0).setVisible(true);
				isNot.get(0).setLocation(l.getLocation().x,l.getLocation().y);
				isNot.remove(0);
				//如果还是落棋阶段，标记此位置不能再落子
				if(count<=9){
					point[lx][ly]="4";
				}
				this.send("lose."+uname+"-"+lx+"~"+ly+"|"+l.getToolTipText().replace("b",""));
				return true;
			}else{ //如果是走棋阶段
				point[lx][ly]="0";
				this.send("lose."+uname+"-"+lx+"~"+ly+"|"+l.getToolTipText().replace("b",""));
				return true;
			}
		}
	}

	//失去棋子
	public void isLose(Label l,int x,int y){
		if(count<=9){ //如果是落棋阶段
			isNot.get(0).setVisible(true);
			isNot.get(0).setLocation(l.getLocation().x,l.getLocation().y);
			isNot.remove(0);
			point[x][y]="4";
		}else{
			point[x][y]="0";
			isGo=true;
		}
		lbs.remove(l);
		l.dispose();
		//如果还是落棋阶段，标记此位置不能再落子
		myscore--;  //我失去一个棋子
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				label_6.setText(myscore+"");
			}
		});
		if(myscore<=2){
			gameOver=true;
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					label_4.setText("很遗憾,您输了....");
				}
			});
			this.send("gameover."+uname+"-1");
			gameOver=true;
			YcUtil.showMsg(shell,"失败提示","很遗憾,您输了.点击棋盘再来一盘....");
		}
	}

	//对方走棋
	public void isMove(Label l,int x,int y){
		y1=x<=2?0:x-2;
		x1=y;

		point[x1][y1]="2";
		numx=4;
		numy=0;

		int px=l.getLocation().x/76; //获取当前Label的x轴
		int py=l.getLocation().y/76; //获取当前Label的y轴

		int lx=0;
		int ly=0;

		ly=px<=2?0:px-2;
		lx=py;

		point[lx][ly]="0";

		//修正坐标
		if(y>=3){
			numy+=8;
		}

		if(x>2&&x<=5){
			numx+=4;
		}

		if(x>5){
			numx+=12;
		}
		l.setBounds(x*76+numx,y*76+numy,44,44);
		boolean rs=isSan(x1,y1,"2");
		if(rs){
			isGo=false;
			label_4.setText("对方已经成三,等待对方吃子...");
		}else{
			isGo=true;
			label_4.setText("对方已走棋,请走棋...");
		}
	}
}
