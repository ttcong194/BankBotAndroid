package vn.aiteam.hackathon.Model;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MessagesFixtures extends FixturesData {
    private MessagesFixtures() {
        throw new AssertionError();
    }


    public static Message getTextMesageOfBot(String content) {
        Message message = new Message(getRandomId(), getBotUser(), content);
        return message;
    }

    public static Message getTextMesageOfLocal(String content) {
        Message message = new Message(getRandomId(), getLocalUser(), content);
        return message;
    }

    public static Message getFileMesageOfLocal(File file) {
        Message message = new Message(getRandomId(), getLocalUser(),null);
        message.setFile(file);
        return message;
    }

    public static Message getImageMesageOfBot(String url){
        Message message = new Message(getRandomId(), getBotUser(), null);
        message.setImage(new Message.Image(url));
        return message;
    }

    public static Message getImageMessage() {
        Message message = new Message(getRandomId(), getUser(), null);
        message.setImage(new Message.Image(getRandomImage()));
        return message;
    }

    public static Message getVoiceMessage() {
        Message message = new Message(getRandomId(), getLocalUser(), null);
        message.setVoice(new Message.Voice("http://example.com", rnd.nextInt(200) + 30));
        return message;
    }



    public static Message getTextMessage() {
        return getTextMessage(getRandomMessage());
    }

    public static Message getTextMessage(String text) {
        return new Message(getRandomId(), getUser(), text);
    }

    public static ArrayList<Message> getMessages(Date startDate) {
        ArrayList<Message> messages = new ArrayList<>();
        for (int i = 0; i < 10/*days count*/; i++) {
            int countPerDay = rnd.nextInt(5) + 1;

            for (int j = 0; j < countPerDay; j++) {
                Message message;
                if (i % 2 == 0 && j % 3 == 0) {
                    message = getImageMessage();
                } else {
                    message = getTextMessage();
                }

                Calendar calendar = Calendar.getInstance();
                if (startDate != null) calendar.setTime(startDate);
                calendar.add(Calendar.DAY_OF_MONTH, -(i * i + 1));

                message.setCreatedAt(calendar.getTime());
                messages.add(message);
            }

        }
        return messages;
    }

    private static User getBotUser() {
        return new User("1", "Chatbot", "ic_bot",true);
    }

    private static User getLocalUser() {
        return new User("0", "Local", "ic_bot",true);
    }


    private static User getUser() {
        boolean even = rnd.nextBoolean();
        return new User(
                even ? "0" : "1",
                even ? names.get(0) : names.get(1),
                even ? avatars.get(0) : avatars.get(1),
                true);
    }
}
