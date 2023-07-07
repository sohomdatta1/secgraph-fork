/*
 * 
 * The MIT License (MIT)
 * Copyright (c) <year> <copyright holders>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
*/
package views;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class CreateAlgoComposit {
    public static Button createAlgoComposit(Composite parentComposite,Composite compositToFill, final Shell shell,
            String[] labelNames,ArrayList<Text> text, Integer[] type){
        if(labelNames.length!=type.length){
            throw new RuntimeException("Inconsistant number of labels and type");
        }
        if(text.size()!=0){
            throw new RuntimeException("Text is not empty");
        }
        
        if(compositToFill!=null){
            for(Control c : compositToFill.getChildren()) {
                c.dispose();
            }
        }else{            
            compositToFill = new Composite(parentComposite, SWT.NONE);
        }
        compositToFill.setLayout(new GridLayout(3, false));
        
        
        for(int i = 0;i<labelNames.length;i++){
            Label label = new Label(compositToFill, SWT.NONE);
            label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
            label.setText(labelNames[i]);
            
            
            switch (type[i]) {
            case SecGraphConstants.NONE:
                final Text textBox0 = new Text(compositToFill, SWT.BORDER | SWT.CENTER);
                textBox0.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
                text.add(textBox0);
                new Label(compositToFill, SWT.NONE);
                break;
            case SecGraphConstants.OPEN:
                final Text textBox1 = new Text(compositToFill, SWT.BORDER | SWT.CENTER);
                textBox1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
                text.add(textBox1);
                Button InputFileButton = new Button(compositToFill, SWT.NONE);
                InputFileButton.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                        String result = dialog.open();
                        textBox1.setText(result);
                    }
                });
                InputFileButton.setText("Select");
                break;
            case SecGraphConstants.SAVE:
                final Text textBox2 = new Text(compositToFill, SWT.BORDER | SWT.CENTER);
                textBox2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
                text.add(textBox2);
                Button output = new Button(compositToFill, SWT.NONE);
                output.setText("Select");
                output.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        FileDialog dialog = new FileDialog(shell, SWT.SAVE);
                        String result = dialog.open();
                        textBox2.setText(result);
                        
                    }
                }); 
                break;
            case SecGraphConstants.MODE:
                final Combo mode = new Combo(compositToFill, SWT.READ_ONLY| SWT.CENTER); 
                mode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
                final String[] modeStrs = { 
                        "stack",
                        "queue",
                };
                for(String s : modeStrs){
                    mode.add(s);
                }
                new Label(compositToFill, SWT.NONE);
                break;
            default:
                break;
            }
        }
        Button start = new Button(compositToFill, SWT.NONE);
        start.setText("Start");
        return start;
    }
}
