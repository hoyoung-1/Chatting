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

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
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

	// clientNet 자원
	private Socket socket;
	private String ip;
	private int port;
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;

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

			ip = ipTf.getText().trim();
			port = Integer.parseInt(portTf.getText().trim());

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
		}

	} // End connection

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

		JList userLt = new JList();
		userLt.setBounds(17, 45, 100, 120);
		contentPane.add(userLt);

		JLabel lbChatRoom = new JLabel("채 팅 방");
		lbChatRoom.setBounds(17, 205, 100, 20);
		lbChatRoom.setFont(new Font("맑은 고딕", Font.BOLD, 18));
		lbChatRoom.setHorizontalAlignment(JLabel.CENTER);
		contentPane.add(lbChatRoom);

		JList chatRoomLt = new JList();
		chatRoomLt.setBounds(17, 230, 100, 120);
		contentPane.add(chatRoomLt);

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

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == connectBtn) {

			System.out.println("loginFrame 접속이 눌림");
			clientNet();

		} else if (e.getSource() == noteSendBtn) {

			System.out.println("쪽지보내기 버튼");

		} else if (e.getSource() == joinRoomBtn) {

			System.out.println("참여 버튼 ");

		} else if (e.getSource() == makeRoomBtn) {

			System.out.println("방만들기 버튼");

		} else if (e.getSource() == sendBtn) {

			System.out.println("전송버튼");

		}
	}

	public static void main(String[] args) {
		new ClientChat(); // 시작될 때 생성자를 호출
	}
}
