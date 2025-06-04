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

    public TermReadEvent(Object source, String term, String path, int fileId) {
        super(source);
        this.term = term;
        this.path = path;
        this.fileId = fileId;
    }
}
