package com.yc.games;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.swtdesigner.SWTResourceManager;
import com.yc.util.YcUtil;

public class Servers {
	protected Shell shell;
	private Button button;  //服务器启动按钮
	private boolean started=false; //标志, 是否启动
	private ServerSocket ss=null;      //服务器套接字
	private List<Clients> clients=new ArrayList<Clients>();   //客户端的集合, 一个服务器可以有多个客户端
	private List<Record> records=new ArrayList<Record>();  //登陆的用户信息
	private Record rd;  //对战信息
	private Table table;
	private Display display;

	private Person pf;
	private Thread t; // 线程对象

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Servers window = new Servers();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();

		//TODO:一秒钟扫描一次personInfos  用户登录 的信息集合，以更新用户列表界面
		new Thread(  new Runnable(){
			@Override
			public void run() {
				while(  true){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					showInfo();
				}
			}
		}).start();

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
		//当shell销毁时
		shell.addDisposeListener(new DisposeListener() {
			@SuppressWarnings("deprecation")
			public void widgetDisposed(DisposeEvent arg0) {
				if(t!=null){
					t.stop();
					display.close();
					System.exit(0);
				}
			}
		});
		shell.setImage(SWTResourceManager.getImage(Servers.class, "/images/qi.png"));
		shell.setSize(599, 402);
		shell.setLocation(Display.getCurrent().getClientArea().width/2-shell.getShell().getSize().x/2, Display.getCurrent().getClientArea().height/2 - shell.getSize().y /2);
		shell.setText("成三棋服务器端");

		button = new Button(shell, SWT.NONE);

		button.setBounds(259, 10, 80, 30);
		button.setText("\u542F\u52A8\u670D\u52A1\u5668");

		Group group = new Group(shell, SWT.NONE);
		group.setText("\u5F53\u524D\u5BA2\u6237\u7AEF\u5217\u8868");
		group.setBounds(10, 48, 584, 343);

		table = new Table(group, SWT.BORDER | SWT.FULL_SELECTION);
		table.setBounds(10, 36, 564, 297);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumn tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setWidth(56);
		tableColumn.setText("  \u5E8F\u53F7");

		TableColumn tableColumn_1 = new TableColumn(table, SWT.CENTER);
		tableColumn_1.setWidth(301);
		tableColumn_1.setText("\u6311\u6218\u65B9\u6635\u79F0");

		TableColumn tableColumn_2 = new TableColumn(table, SWT.CENTER);
		tableColumn_2.setWidth(200);
		tableColumn_2.setText("\u5E94\u6218\u65B9\u6635\u79F0");

		//点击启动服务器
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				t=new Thread(){
					public void run(){
						startServer();
					}
				};
				t.start();
			}
		});

		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(final ShellEvent e){
				started=false;
				display.close();
				System.exit(0);
			}
		});
	}

	/**
	 * 1. 创建ServerSocket对象
	 * 2. 设置启动状态 started为true
	 * 3. 死循环接收客户机的联接
	 * 4. 每联接一个客户机，形成一个Clients对象, 加入到clients集合中    ->   创建一个线程Thread,用于操作Clients
	 */
	public void startServer(){
		try {
			ss=new ServerSocket(8899);
			started=true;
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					button.setText("服务器已启动");
					button.setEnabled(false);
				}
			});
		} catch (IOException e) {
			started=false;
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					button.setText("启动服务器");
					button.setEnabled(true);
				}
			});
			YcUtil.showMsg(shell,"失败提示","服务器启动失败,可能是端口被占用...");
			return;
		}
		try {
			while(started){
				Socket s=ss.accept();    //接收一个与客户机的联接
				Clients cc=new Clients(s);    //cc是一个runnable,  必须放在Thread对象
				Thread t=new Thread(cc );
				t.start();
				clients.add(cc); //将客户端信息存入集合
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{//服务器关机
			try {
				ss.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 内部类cliects: 表示与一个客户端的联接
	 */
	public class Clients implements Runnable{
		Socket s;    //通过socket与客户机联接
		DataInputStream dis;   //输入流，用于从客户端读取数据
		DataOutputStream dos;  //输出流，用于向客户端输入数据
		boolean connected=false;//表示客户机与服务器的联接标识
		boolean flag=true;  //判断是新建对战还是迎战
		public Person p; //当前用户信息


		//构造方法:建立与客户端的联接
		public Clients(Socket s){
			try {
				this.s=s;  
				dis=new DataInputStream( this.s.getInputStream() );
				dos=new DataOutputStream(this.s.getOutputStream() );

				String info=dis.readUTF();
				if(info.startsWith("userInfo.")){  //如果是新添用户信息 userInfo.张三-李四
					pf=new Person();
					pf.setName(info.substring(info.indexOf(".")+1,info.indexOf("-")));
					pf.setOtherName(info.substring(info.indexOf("-")+1));

					//验证该用户信息是否存在
					for(Record rs:records){
						//当输入用户的名字跟用户表用户中对战用户名字相同时，说明此两人要对战
						if(pf.getName().equalsIgnoreCase(rs.getOtherName())){
							this.send("wait."); //告诉自己等待对方落子
							for(Clients cc:clients){    //循环集合，取出每一个clientcon对象
								if(cc.p.getName().equals(pf.getOtherName())){  //告诉对方开始落子
									cc.send("ready.");
								}
							}
							flag=false;  //说明是开战，不是新增挑战信息
							break;
						}else if(rs.getName().equals(pf.getName())||rs.getOtherName().equals(pf.getName())||rs.getOtherName().equals(pf.getOtherName())){//说明该用户已经在对战
							send("error."); //告诉客户端该用户已经在对战
							connected=false;  //客户机与服务器连接失败
							return;
						}
					}

					this.p=pf;//保存当前对象

					if(flag){ //如果是新增对战信息
						rd=new Record(pf);
						records.add(rd);
						rd=null;
					}
				}
				connected=true;   //已经建立了联接...
			} catch (IOException e) {
				YcUtil.showMsg(shell,"错误提示","建立与客户端的联系失败...");
			}
		}

		/**
		 * 服务器向客户机发送信息
		 */
		public void send(String info){
			try {
				dos.writeUTF(info);
				dos.flush();
			} catch (IOException e) { //如果客户机掉线
				clients.remove(this);
				rd=new Record(this.p.getOtherName(),this.p.getName());
				records.remove(rd);
				rd=new Record(this.p.getName(),this.p.getOtherName());
				records.remove(rd);
			}
		}

		@Override    //这个表示服务器端被动的接收客户端传过来的信息    
		/**
		 * 1. 判断是否与客户机联接     2. 收信息   dis收   dis.readUTF()        3. 向其它的客户机群发这个信息       
		 * 如果客户机掉线了，   从clients集合中移除这个客户机   
		 */
		public void run() {
			String uname="";
			try {
				while(connected){
					String info=dis.readUTF(); //接收客服端的信息
					if(info.startsWith("data.")){ //获取客户端落子信息
						uname=info.substring(info.indexOf(".")+1,info.indexOf("-")); 
						String temp=info.substring(info.indexOf("-")+1); //坐标信息及落子信息

						//发送给对方
						for(Clients cc:clients){    //循环集合，取出每一个clients对象
							if(cc.p.getOtherName().equals(uname)){
								cc.send("dataInfo."+temp); 
							}
						}

					}else if(info.startsWith("gameover.")){
						uname=info.substring(info.indexOf(".")+1,info.indexOf("-")); 
						String temp=info.substring(info.indexOf("-")+1); //坐标信息及落子信息

						//发送给对方
						for(Clients cc:clients){    //循环集合，取出每一个clients对象
							if(cc.p.getOtherName().equals(uname)){
								cc.send("gameover."+temp);  //lose.b1~lx|ly
							}
						}
						
					}else if(info.startsWith("lose.")){ //获取客户端落子信息
						uname=info.substring(info.indexOf(".")+1,info.indexOf("-")); 
						String temp=info.substring(info.indexOf("-")+1); //坐标信息及落子信息

						//发送给对方
						for(Clients cc:clients){    //循环集合，取出每一个clients对象
							if(cc.p.getOtherName().equals(uname)){
								cc.send("lose."+temp);  //lose.b1~lx|ly
							}
						}
					}if(info.startsWith("move.")){ //获取客户端落子信息
						uname=info.substring(info.indexOf(".")+1,info.indexOf("-")); 
						String temp=info.substring(info.indexOf("-")+1); //坐标信息及落子信息

						//发送给对方
						for(Clients cc:clients){    //循环集合，取出每一个clients对象
							if(cc.p.getOtherName().equals(uname)){
								cc.send("move."+temp);  //lose.b1~lx|ly
							}
						}
					}else if(info.startsWith("end.")){ //用户下线
						String endname=info.substring(info.indexOf(".")+1); //获取下线的用户名
						for(Clients cc:clients){    //循环集合，取出每一个clients对象
							if(cc.p.getName().equals(endname)){
								cc.send("end."); //调用cc中的send方法，发送信息，告诉客服端可以下线了
							}
							if(cc.p.getOtherName().equals(endname)){
								cc.send("otherend."); //调用cc中的send方法，发送信息，告诉客户端，对方已经下线了
							}
						}

						for(Record r:records){
							if(r.getName().equals(endname)||r.getOtherName().equals(endname)){
								records.remove(p);
								break;
							}
						}
					}
				}
			} catch (IOException e) {
				System.out.println("服务器操作日志: 在接收信息时,  客户端:"+s.getInetAddress().getHostAddress()+"已经掉线了   时间:"+ YcUtil.getCurrentTime()  );
				clients.remove(this);
				rd=new Record(this.p.getOtherName(),this.p.getName());
				records.remove(rd);
				rd=new Record(this.p.getName(),this.p.getOtherName());
				records.remove(rd);
			}finally{
				if( s!=null ){
					try {
						s.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void showInfo(){
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				table.removeAll();
				TableItem ti=null;
				if(records!=null&&records.size()>0){
					for(int i=0;i<records.size();i++){
						ti=new TableItem(table,SWT.NONE);
						ti.setText(new String[]{i+1+"",records.get(i).getName(),records.get(i).getOtherName()});
					}
				}
			}
		});
	}
}
