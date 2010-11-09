/*
 * Copyright (c) 2009 Nicholas C. Zakas. All rights reserved.
 * http://www.nczonline.net/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.nczonline.web.cssembed;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;

/**
 * Ant task for CSS Embed. See cssembed.xml for usage.
 * @author Phil Mander
 */
public class CSSEmbedAntTask extends MatchingTask
{   
    protected File fromdir;
    protected File destdir;    
    private String suffix = "";
    private String root = null;
    private String charset = "UTF-8";    
    private boolean verbose = false;   
    private boolean mhtml = false;
    
    //--------------------------------------------------------------------------
    // Attributes
    //--------------------------------------------------------------------------    
    
    /**
     * The base directory to scan for files to process. Mandatory
     * @param fromdir
     */
    public void setFromdir(File fromdir)
    {
        this.fromdir = fromdir;
    }

    /**
     * The directory to output files to. Mandatory
     * @param destdir
     */
    public void setDestdir(File destdir)
    {
        this.destdir = destdir;
    }

    /**
     * A suffix to append to output file names. 
     * @param suffix
     */
    public void setSuffix(String suffix)
    {
        this.suffix = suffix;
    }
    
    /**
     * Verbose output. Defaults to false.
     * @param verbose
     */
    public void setVerbose(boolean verbose)
    {
        this.verbose = verbose;
    }

    /**
     * The root for relative image paths.
     * @param root
     */
    public void setRoot(String root)
    {
        this.root = root;
    }

    /**
     * Charset. Defaults to UTF-8
     * @param charset
     */
    public void setCharset(String charset)
    {
        this.charset = charset;
    }
   
    /**
     * A suffix to append to output file names. 
     * @param suffix
     */
    public void setMhtml(boolean mhtml)
    {
        this.mhtml = mhtml;
    }

    //--------------------------------------------------------------------------
    // Execute
    //--------------------------------------------------------------------------    

    @Override
    public void execute() throws BuildException
    {
        checkAttributes();
        
        DirectoryScanner dirScanner = getDirectoryScanner(fromdir);
        String[] files = dirScanner.getIncludedFiles();
        
        int options = CSSURLEmbedder.DATAURI_OPTION;
        
        if(mhtml)
        {
            options = CSSURLEmbedder.MHTML_OPTION;
        }

        log("CSSEmbedding " + files.length + "file from " + fromdir.getAbsoluteFile().toString());
        
        for (int i = 0; i < files.length; i++)
        {            
            String inFileName = files[i];           
            
            //update output suffix
            String ext = inFileName.substring(inFileName.lastIndexOf("."), inFileName.length());
            String outFileName = inFileName.replace(ext, suffix + ext);
            
            File inFile = new File(fromdir.getAbsolutePath(), inFileName);
            File outFile = new File(destdir.getAbsolutePath(), outFileName);
            
            Reader in = null;
            Writer out = null;          
            try
            {
                in = new InputStreamReader(new FileInputStream(inFile), charset);
                out = new OutputStreamWriter(new FileOutputStream(outFile), charset);
          
                CSSURLEmbedder embedder = new CSSURLEmbedder(in, options, verbose);   
 
                //close in case writing to the same file
                in.close();
                in = null;
                
                embedder.setFilename(outFile.getName());
                
                log("Reading " + inFile.toString());
                embedder.embedImages(out, root);
                log("Written output to " + outFile.toString());
            }
            catch (UnsupportedEncodingException e)
            {
                throw new BuildException(e);
            }
            catch (IOException e)
            {
                throw new BuildException(e);
            }
            finally
            {
                if(out != null)
                {
                    try
                    {
                        out.flush();
                        out.close();
                    }
                    catch (IOException e)
                    { 
                        e.printStackTrace();
                    }
                    
                }                
            }
            
            log(files.length + " written to " + destdir.getAbsoluteFile().toString());
        }
    }
    
    private void checkAttributes()
    {
        String message = null;
        
        if(fromdir == null)
        {
            message = "Missing fromdir attribute";
        }
        if(destdir == null)
        {
            message = "Missing destdir attribute";
        }
        
        if(message != null)
        {
            throw new BuildException(message);
        }        
    }
}
