package dz.folderprocessor.events;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class BigramReadEvent extends ApplicationEvent {

    private String bigram;
    private String path;
    private int fileId;
    private int position;

    public BigramReadEvent(Object source, String bigram, String path, int fileId, int position) {
        super(source);
        this.bigram = bigram;
        this.path = path;
        this.fileId = fileId;
        this.position = position;
    }
}