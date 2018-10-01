package gov.nih.nlm.malaria_screener.custom;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Created by yuh5 on 1/25/2018.
 */

public class Memory {

    /**
     * Function that get the size of an object.
     *
     * @param object
     * @return Size in bytes of the object or -1 if the object
     * is null.
     * @throws IOException
     */
    public static final int sizeOf(Object object) throws IOException {

        if (object == null)
            return -1;

        // Special output stream use to write the content
        // of an output stream to an internal byte array.
        ByteArrayOutputStream byteArrayOutputStream =
                new ByteArrayOutputStream();

        // Output stream that can write object
        ObjectOutputStream objectOutputStream =
                new ObjectOutputStream(byteArrayOutputStream);

        // Write object and close the output stream
        objectOutputStream.writeObject(object);
        objectOutputStream.flush();
        objectOutputStream.close();

        // Get the byte array
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        // TODO can the toByteArray() method return a
        // null array ?
        return byteArray == null ? 0 : byteArray.length;


    }


}
