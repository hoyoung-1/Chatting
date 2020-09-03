package server.gui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
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

	ServerChat() { // 생성자
		serverInit();
		actionBtn();
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

		this.setVisible(true); // 상속받은 JFrame이 보이도록
	} // End serverInit();

	// Socket 설정

	private void serverNet() {
		try {
			port = Integer.parseInt(portTf.getText().trim());
			serverSocket = new ServerSocket(port); // 포트번호 부여

			if (serverSocket != null) {
				connection();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	} // End serverNet();

	private void connection() {
		
		Thread th = new Thread(new Runnable() {
			// 1개의 스레드에서 1가지의 기능만 
			@Override
			public void run() {
				try {
					textArea.append("접속 대기중.....\n");
					socket = serverSocket.accept();
					textArea.append("사용자 접속\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		th.start(); // 이 부분을 넣어야함
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == startBtn) {

			System.out.println("서버 시작이 눌림");
			serverNet();

		} else if (e.getSource() == stopBtn) {

			System.out.println("서버 중단이 눌림");

		} // End if

	} // End actionPerformed

	public static void main(String[] args) {
		new ServerChat();
	} // End main

} // End ServerCaht
