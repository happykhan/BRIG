/* Copyright Nabil Alikhan. 2010.
 * This file is part of BLAST Ring Image Generator (BRIG).
 * BRIG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BRIG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 *  along with BRIG.  If not, see <http://www.gnu.org/licenses/>.
 */


package brig;

/**
 *
 * @author nabil
 */
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;

/**
 * This class extends the OutputStream class to allow output to be printed
 * to a TextArea ...
 */
public class TextAreaOutputStream extends OutputStream
{
  private JTextArea textArea;
  private StringBuffer strBuffer = new StringBuffer ();
  private boolean closed = false;

  // private byte savedByte;
  // private boolean savedByteAvailable = false;

  public TextAreaOutputStream (JTextArea textArea)
  {
    super ();
    this.textArea = textArea;
  }

  /** method to write a char */
  public void write (int i)
       throws IOException
  {
    if (closed)
      return;

    strBuffer.append ( (char) i);

  }

  /** write an array of bytes */
  public void write (byte[] b, int offset, int length)
       throws IOException
  {
    if (closed)
      return;

    if (b == null)
      throw new NullPointerException ("The byte array is null");
    if (offset < 0 || length < 0 || (offset+length) > b.length)
      throw new IndexOutOfBoundsException ("offset and length are negative or extend outside array bounds");

    String str = new String (b, offset, length);
    strBuffer.append (str);
  }

  /*
   * *****************************************************************
   private void oldWrongWrite (int i)
   {
   if (closed)
   return;

   byte b = (byte) i;
   if (savedByteAvailable) {
   // concat the bytes together & append the char to the strBuffer
   char c = (char) ((savedByte << 8) | b);
   strBuffer.append (c);
   savedByteAvailable = false;
   }
   else {
   savedByte = b;
   savedByteAvailable = true;
   }
   }
   * *****************************************************************
   */

  /** flush (from OutputStream) */
  public void flush ()
  {
    if (closed || strBuffer.length() == 0)
      return;

    textArea.append (strBuffer.toString () );
    int length = strBuffer.length ();
    strBuffer.delete (0, length);
  }

  public void close ()
  {
    flush ();
    strBuffer = null;
    textArea = null;
    closed = true;
  }

}

