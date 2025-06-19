package dz.folderprocessor.events;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class TermReadEvent extends ApplicationEvent {

    private String term;
    private String path;
    private int fileId;
    private int position;

    public TermReadEvent(Object source, String term, String path, int fileId, int position) {
        super(source);
        this.term = term;
        this.path = path;
        this.fileId = fileId;
        this.position = position;
    }
}
