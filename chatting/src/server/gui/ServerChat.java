package server.gui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class ServerChat extends JFrame implements ActionListener {

	// serverChat의 자원
	private JPanel contentPane;
	private JTextField portTf;
	private JTextArea textArea = new JTextArea(); // 서버 log 확인 textArea
	private JButton startBtn = new JButton("서버 시작");
	private JButton stopBtn = new JButton("서버 중단");

	// serverNet 자원
	private ServerSocket serverSocket;
	private Socket socket;
	private int port;
	private Vector userVector = new Vector();
	private Vector roomVector = new Vector();

	private StringTokenizer st;

	ServerChat() { // 생성자
		serverInit();
		actionBtn();
	}

	private void serverNet() { // 서버 접속
		try {
			port = Integer.parseInt(portTf.getText().trim());
			serverSocket = new ServerSocket(port); // 포트번호 부여

		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "서버 실행에 오류가 발생했습니다.", "알림", JOptionPane.ERROR_MESSAGE, null);
		}

		if (serverSocket != null) {
			connection();
		}

	} // End serverNet();

	private void connection() {

		// thread를 처리하기 전에 먼저 IOStream을 설정

		Thread th = new Thread(new Runnable() {
			// 1개의 스레드에서 1가지의 기능만
			@Override
			public void run() {

				while (true) { // 사용자를 계속해서 받아줌

					try {

						textArea.append("접속 대기중.....\n");
						socket = serverSocket.accept();
						textArea.append("사용자 접속\n");

						UserInfo user = new UserInfo(socket); // 소켓을 객체로 만듬

						user.start(); // thread를 상속받았기 때문에 가능

					} catch (IOException e) {

						break;
					}
				}
			}
		});

		th.start(); // 이 부분을 넣어야함
	}

	private void actionBtn() { // 이벤트 등록 메소드
		startBtn.addActionListener(this);
		stopBtn.addActionListener(this);
	} // End actionBtn();

	private void serverInit() { // 화면 GUI

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 460, 470);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(21, 15, 400, 300);
		contentPane.add(scrollPane);

		scrollPane.setViewportView(textArea);
		textArea.setEditable(false);

		JLabel lbPort = new JLabel("포트번호");
		lbPort.setFont(new Font("맑은 고딕", Font.BOLD, 18));
		lbPort.setBounds(21, 330, 82, 21);
		contentPane.add(lbPort);

		portTf = new JTextField();
		portTf.setBounds(122, 327, 299, 27);
		contentPane.add(portTf);
		portTf.setColumns(10);

		startBtn.setBounds(21, 365, 180, 29);
		contentPane.add(startBtn);

		stopBtn.setBounds(241, 365, 180, 29);
		contentPane.add(stopBtn);
		stopBtn.setEnabled(false);

		this.setVisible(true); // 상속받은 JFrame이 보이도록
	} // End serverInit();

	// Socket 설정

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == startBtn) {

			System.out.println("서버 시작이 눌림");
			serverNet();
			startBtn.setEnabled(false);
			portTf.setEditable(false);

			stopBtn.setEnabled(true);

		} else if (e.getSource() == stopBtn) {
			startBtn.setEnabled(true);
			portTf.setEditable(true);
			portTf.setText("");
			System.out.println("서버 중단이 눌림");
			try {
				// 서버에 대한 모든 자원을 초기화 시킴
				serverSocket.close();
				userVector.removeAllElements();
				roomVector.removeAllElements();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		} // End if

	} // End actionPerformed

	public static void main(String[] args) {
		new ServerChat();
	} // End main

	class UserInfo extends Thread {

		private OutputStream os;
		private InputStream is;
		private DataOutputStream dos;
		private DataInputStream dis;

		private Socket userSocket;
		private String name;

		private boolean roomCh = true; // 방을 만들 수 있는 상태

		UserInfo(Socket socket) {
			this.userSocket = socket; // 연결된 소켓을 객체로 만듬 .
			userNet();
		}

		// IOStream
		private void userNet() {

			try {

				is = userSocket.getInputStream(); // 그냥 소켓으로하면 전역변수의 소켓을 사용하기 때문에 userScoket을 켜야함
				dis = new DataInputStream(is);

				os = userSocket.getOutputStream();
				dos = new DataOutputStream(os);

				name = dis.readUTF();
				textArea.append(name + "님이 접속하였습니다.\n");

				// 기존 사용자들에게 새로운 사용자 알림

				System.out.println("현재 접속된 사용자 수 : " + userVector.size());

				broadCast("NewUser/" + name);

				// 자신에게 기존 사용자를 알림

				for (int i = 0; i < userVector.size(); i++) {
					UserInfo u = (UserInfo) userVector.elementAt(i);

					sendMessage("OldUser/" + u.name);
				}

				// 자신에게 기존 방을 알림

				for (int i = 0; i < roomVector.size(); i++) {
					RoomInfo r = (RoomInfo) roomVector.elementAt(i);

					sendMessage("OldRoom/" + r.roomName);
				}

				sendMessage("RoomListUpdate/ ");

				userVector.add(this); // 사용자들에게 알린 후에 vector에 자신을 추가

				broadCast("UserListUpdate/ ");
				broadCast("RoomListUpdate/ ");

			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Strame 설정 오류", "알림", JOptionPane.ERROR_MESSAGE, null);

			}

		}// End userNet();

		public void run() { // thread로 처리할 내용
			// 클라이언트와 연결된 메세지가 들어오는 곳을 개별 스레드를 돌려서 계속 메세지를 받음

			try {
				while (true) {

					String msg = dis.readUTF();
					textArea.append(name + ": 사용자로부터 들어온 메세지 : " + msg + "\n");
					inMessage(msg);
				}
			} catch (IOException e) {

				textArea.append(name + "님이 퇴장하셨습니다.\n");
				try {
					dos.close();
					dis.close();
					userSocket.close();

					userVector.remove(this);
					broadCast("UserOut/" + name);
					broadCast("UserListUpdate/ ");


				} catch (IOException e1) {

				}
			}

		}// End run();

		private void inMessage(String str) {

			st = new StringTokenizer(str, "/");

			String protocol = st.nextToken();
			String user = st.nextToken();

			System.out.println("protocol : " + protocol);
			System.out.println("메세지 : " + user);

			// 메세지를 받아서 protocol이 Note라면 다시 보냄
			if (protocol.equals("Note")) {

				String note = st.nextToken();

				System.out.println("받는 사람 : " + user);
				System.out.println("쪽지 : " + note);

				// 백터에서 해당 사용자를 찾아서 메세지 전송
				for (int i = 0; i < userVector.size(); i++) {
					UserInfo u = (UserInfo) userVector.elementAt(i);

					if (u.name.equals(user)) { // 받는 사람이름을 백터에서 찾아서 있다면 보냄
						u.sendMessage("Note/" + name + "/" + note);
					}
				}

			} else if (protocol.equals("CreateRoom")) { // 채팅방 만들기

				// 1. 현재 같은 방이 존재하는지 확인한다
				for (int i = 0; i < roomVector.size(); i++) {
					RoomInfo r = (RoomInfo) roomVector.elementAt(i);

					if (r.roomName.equals(user)) {// 만들고자 하는 방이 존재를 할 때.
						sendMessage("CreateRoomFail/ok");
						roomCh = false;
						break;
					}

				} // End for

				if (roomCh) { // 방을 만들 수 있을 때. 중복하는 방이 없을 때.
					RoomInfo newRom = new RoomInfo(user, this);
					roomVector.add(newRom); // 전체 방 백터에 방을 추가

					sendMessage("CreateRoom/" + user);

					broadCast("NewRoom/" + user);
				}

				roomCh = true; // 방만들기 실패 후에 다시 초기값으로 돌려줌

			} else if (protocol.equals("Chatting")) {
				String msg = st.nextToken();

				for (int i = 0; i < roomVector.size(); i++) {
					RoomInfo r = (RoomInfo) roomVector.elementAt(i);

					if (r.roomName.equals(user)) {
						r.boradCastRoom("Chatting/" + name + "/" + msg);
					}

				}

			} else if (protocol.equals("JoinRoom")) {

				for (int i = 0; i < roomVector.size(); i++) {
					RoomInfo r = (RoomInfo) roomVector.elementAt(i);

					if (r.roomName.equals(user)) {

						// 새로운 사용자를 알린다.
						r.boradCastRoom("Chatting/알림/**********" + name + "님이 입장하셨습니다**********");

						// 사용자 추가
						r.addUser(this);
						sendMessage("JoinRoom/" + user);
					}
				}

			}

		}

		private void broadCast(String str) { // 전체 사용자에게 메세지를 보내는 부분

			for (int i = 0; i < userVector.size(); i++) {// 현재 접속된 사용자에게 새로운 알림

				UserInfo u = (UserInfo) userVector.elementAt(i);
				System.out.println("broadCast : " + str);
				u.sendMessage(str); // protocol은 NewUser/ 기존의 사용자에게 알림

			}
		}

		private void sendMessage(String str) {

			try {

				dos.writeUTF(str); // 서버에서 나가는 OutputStrame

			} catch (IOException e) {

				JOptionPane.showMessageDialog(null, "채팅방이 닫힘.");

			}
		}

	} // End UserInfo class

	class RoomInfo { // 채팅방 목록
		private String roomName;
		private Vector roomUserVector = new Vector();

		RoomInfo(String str, UserInfo u) {
			this.roomName = str;
			this.roomUserVector.add(u);
		}

		public void boradCastRoom(String str) { // 현재 채팅방의 모든 사용자에게 알린다
			for (int i = 0; i < roomUserVector.size(); i++) {
				UserInfo u = (UserInfo) roomUserVector.elementAt(i);
				u.sendMessage(str);
			}
		}

		public void addUser(UserInfo u) {
			this.roomUserVector.add(u);
		}
		
	}
} // End ServerCaht
