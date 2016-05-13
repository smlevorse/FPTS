import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * @author Steven Teplica
 *         <p>
 *         A custom textfield class that has placeholder text as "********"
 */
public class JPasswordPlaceholder extends JPasswordField {

    private boolean textWrittenIn = false;

    public JPasswordPlaceholder() {
        setPlaceholder("********");
        setBackground(Color.white);
    }

    public void setPlaceholder(final String text) {

        customizeText(text, true);

        getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                warn();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                warn();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                warn();
            }

            public void warn() {
                if (getText().trim().length() != 0) {
                    textWrittenIn = true;
                    setForeground(new Color(50, 50, 50));
                }
            }
        });

        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (!textWrittenIn) {
                    customizeText("", false);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (getText().trim().length() == 0) {
                    customizeText(text, true);
                }
            }
        });
    }

    private void customizeText(String text, boolean i) {
        setText(text);
        if (i) {
            setFont(new Font(getFont().getFamily(), Font.ITALIC, getFont().getSize()));
        } else {
            setFont(new Font(getFont().getFamily(), Font.PLAIN, getFont().getSize()));
        }
        setForeground(new Color(160, 160, 160));
        textWrittenIn = false;
    }
}