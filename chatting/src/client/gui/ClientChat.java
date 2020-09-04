package client.gui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class ClientChat extends JFrame implements ActionListener {

	// loginFrame 자원
	private JFrame loginFrame = new JFrame(); // sub Frame
	private JPanel loginContentPane;
	private JTextField ipTf;
	private JTextField portTf;
	private JTextField nameTf;
	private JButton connectBtn = new JButton("접속");

	// chatFrame 자원
	private JPanel contentPane;
	private JTextField textField;
	private JButton noteSendBtn = new JButton("쪽지보내기");
	private JButton joinRoomBtn = new JButton("참여하기");
	private JButton makeRoomBtn = new JButton("방만들기");
	private JButton sendBtn = new JButton("전송");
	private JList userLt = new JList();
	private JList chatRoomLt = new JList();

	// clientNet 자원
	private Socket socket;
	private String ip;
	private int port;
	private String name;
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;

	// 그 외 자원
	Vector userList= new Vector(); // 접속자 명단
	Vector roomList = new Vector(); // 채팅방 목록 
	StringTokenizer st; // StringTokenizer는 문자열을 어떤 기준으로 나누는데 사용할 수 있는 클래스이다. 
						// 예 ) NewUser/name = StringTokenizer를 사용하면 '/'를 기준으로 NewUser와 name으로 나눌 수 있음

	ClientChat() { // 생성자
		loginFrameInit(); // loginFrame을 가져옴
		chatFrameInit();
		actionBtn();
	}

	private void actionBtn() {
		connectBtn.addActionListener(this);
		noteSendBtn.addActionListener(this);
		joinRoomBtn.addActionListener(this);
		makeRoomBtn.addActionListener(this);
		sendBtn.addActionListener(this);
	}

	private void clientNet() {
		try {

			socket = new Socket(ip, port);
			System.out.println("채팅에 접속됨");

			if (socket != null) {// 정상적으로 소켓이 연결이 되었을 때

				connection();

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} // End clinetNet
	

	private void connection() { // 메소드 연결부분

		try {

			is = socket.getInputStream(); // String 설정상 에러가 발생할수 있음
			dis = new DataInputStream(is);

			os = socket.getOutputStream();
			dos = new DataOutputStream(os);

		} catch (Exception e) { // 에러처리 부분
			e.printStackTrace();
		}// Stream 설정 끝
		
		//처음 접속시 name전송
		sendMessage(name);
		
		// user JList에 vector추가 
		userList.add(name); // 유저만 추가
		
		
		
		// Thread로 멈추지 않게 설정 
		Thread th = new Thread(new Runnable() {
			
			@Override
			public void run() {

				while(true) { // 무한 대기 
					try {
						
						String msg = dis.readUTF();// 메세지 수신
						
						System.out.println("서버로 수신된 메세지 : " + msg);
						
						inMessage(msg);
						
					} catch (IOException e) {

						e.printStackTrace();
					
					} 
				}// End While
			}
		});
		
		th.start();

	} // End connection
	
	private void inMessage(String str) { // 서버로부터 받는 모든 메세지가 여기로 들어옴 
		
		st =new StringTokenizer(str,"/");
		
		String protocol = st.nextToken();
		String user = st.nextToken(); 
		
		System.out.println("프로토콜 : "+ protocol);
		System.out.println("message : " + user);
		
		if(protocol.equals("NewUser")) { // 새로운 접속자 
			
			userList.add(user);
		
			/*
			userLt.setListData(userList);
			userLt.updateUI(); // 빠르게 추가될경우 에러가 발생하기 때문에 변경 
			*/
			//업데이트가 아니라 사용자를 추가 
			
		}else if(protocol.equals("OldUser")) {
			userList.add(user);
			//userLt.setListData(userList); 사용자만 추가 시키기로 
			
		}else if(protocol.equals("Note")) {
		
			String note = st.nextToken();
			
			System.out.println(user + " : " + note);
			
			JOptionPane.showMessageDialog(null, note, user+"로부터 온 쪽지 : ",JOptionPane.CLOSED_OPTION);
		
		}else if(protocol.equals("UserListUpdate")) {
			
			userLt.setListData(userList);
		
		}
		
		
	}

	private void sendMessage(String str) { // 서버에게 메세지를 보내는 메소드, 서버에게 메시지를 보낼 때는 OUT으로 보냄

		try {
			dos.writeUTF(str); // String으로 에러가 발생할 수 있음
		} catch (IOException e) { // 에러처리
			e.printStackTrace();
		}
	}

	private void loginFrameInit() {

		loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		loginFrame.setBounds(100, 100, 400, 500);
		loginContentPane = new JPanel();
		loginContentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		loginFrame.setContentPane(loginContentPane);
		loginContentPane.setLayout(null);

		JLabel lbLoginTitle = new JLabel("채팅");
		lbLoginTitle.setFont(new Font("맑은 고딕", Font.BOLD, 30));
		lbLoginTitle.setBounds(40, 25, 290, 50);
		lbLoginTitle.setHorizontalAlignment(JLabel.CENTER);
		loginContentPane.add(lbLoginTitle);

		JLabel lbIp = new JLabel("IP");
		lbIp.setBounds(40, 128, 82, 21);
		loginContentPane.add(lbIp);

		ipTf = new JTextField();
		ipTf.setBounds(164, 125, 166, 27);
		loginContentPane.add(ipTf);
		ipTf.setColumns(10);

		JLabel lbPort = new JLabel("Port");
		lbPort.setBounds(40, 194, 82, 21);
		loginContentPane.add(lbPort);

		portTf = new JTextField();
		portTf.setBounds(164, 191, 166, 27);
		loginContentPane.add(portTf);
		portTf.setColumns(10);

		JLabel lbName = new JLabel("Name");
		lbName.setBounds(40, 269, 82, 21);
		loginContentPane.add(lbName);

		nameTf = new JTextField();
		nameTf.setBounds(164, 266, 166, 27);
		loginContentPane.add(nameTf);
		nameTf.setColumns(10);

		connectBtn.setBounds(40, 365, 290, 29);
		loginContentPane.add(connectBtn);

		loginFrame.setVisible(true);
	}

	private void chatFrameInit() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 500);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lbUserList = new JLabel("접 속 자");
		lbUserList.setFont(new Font("맑은 고딕", Font.BOLD, 18));
		lbUserList.setHorizontalAlignment(JLabel.CENTER);
		lbUserList.setBounds(17, 15, 100, 20);
		contentPane.add(lbUserList);

		noteSendBtn.setBounds(17, 169, 100, 30);
		contentPane.add(noteSendBtn);

		userLt.setBounds(17, 45, 100, 120);
		contentPane.add(userLt);
		userLt.setListData(userList);

		JLabel lbChatRoom = new JLabel("채 팅 방");
		lbChatRoom.setBounds(17, 205, 100, 20);
		lbChatRoom.setFont(new Font("맑은 고딕", Font.BOLD, 18));
		lbChatRoom.setHorizontalAlignment(JLabel.CENTER);
		contentPane.add(lbChatRoom);

		chatRoomLt.setBounds(17, 230, 100, 120);
		contentPane.add(chatRoomLt);
		chatRoomLt.setListData(roomList);

		joinRoomBtn.setBounds(17, 365, 100, 30);
		contentPane.add(joinRoomBtn);

		makeRoomBtn.setBounds(17, 399, 100, 30);
		contentPane.add(makeRoomBtn);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(134, 22, 427, 373);
		contentPane.add(scrollPane);

		JTextArea textArea = new JTextArea();
		scrollPane.setViewportView(textArea);

		textField = new JTextField();
		textField.setBounds(134, 401, 330, 27);
		contentPane.add(textField);
		textField.setColumns(10);

		sendBtn.setBounds(473, 400, 88, 29);
		contentPane.add(sendBtn);

		this.setVisible(true);
	}

	
	// 이벤트 작성 부분 
	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == connectBtn) {

			System.out.println("loginFrame 접속이 눌림");
			
			ip = ipTf.getText().trim();
			port = Integer.parseInt(portTf.getText().trim());
			name = nameTf.getText().trim(); // name을 받아오는 부분 
			
			clientNet();

		} else if (e.getSource() == noteSendBtn) {

			System.out.println("쪽지보내기 버튼");
			String user = (String)userLt.getSelectedValue();
			
			String note = JOptionPane.showInputDialog("보낼메세지");
			
			if(note != null) { // 사용자가 입력했을 때

				// ex) Note/user2/안녕하세요 
				sendMessage("Note/"+user+"/"+note);
				
			}
			
			System.out.println("받는사람 : " + user);
			System.out.println("쪽지 내용 : " + note);
			

		} else if (e.getSource() == joinRoomBtn) {

			System.out.println("참여 버튼 ");

		} else if (e.getSource() == makeRoomBtn) {

			System.out.println("방만들기 버튼");

		} else if (e.getSource() == sendBtn) {

			System.out.println("전송버튼");
			sendMessage("임시테스트 입니다.");

		} 
	}

	public static void main(String[] args) {
		new ClientChat(); // 시작될 때 생성자를 호출
	}
}
