-> BankBot là chatbot tư vấn các dịch vụ của ngân hàng. Chatbot này tham gia cuộc thi AI Hackathon 2019 tại Hà Nội (https://youtu.be/I-2hZ6nZPn4)

-Server:

Sử dụng mô hình retrieval model, gồm các story, intent đã được định nghĩa sẵn cho domain ngân hàng SHB <br />
Server xây dựng sử dụng RASA Framework và deploy lên linux của Google Cloud

-Client:
Ứng dụng Android sử dụng thư viện chat là Chatkit, Google Text-To-Speech (TTS), Google Speech-To-Text (STT)

Chatkit tham khảo tại địa chỉ sau: https://github.com/stfalcon-studio/ChatKit <br />
TTS tham khảo tại địa chỉ sau: https://github.com/changemyminds/Google-Cloud-TTS-Android <br />
STT tham khảo tại địa chỉ sau : https://cloud.google.com/speech-to-text/docs/samples <br />



Chức năng cơ bản:

- Tra cứ tỷ giá tiền gửi
- Khóa mở thẻ
- Mở thẻ tín dụng
- Thông báo lỗi chuyển tiền

Note: để chạy ứng dụng bạn cần file credetail.json và API Key từ dịch vụ TTS và STT trong tài khoản của bạn ở cloud.google.com
