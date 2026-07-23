import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

class MyClassLoader extends ClassLoader {
    private String classPath;

    public MyClassLoader(String classPath) {
        this.classPath = classPath;
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            String filePath = classPath + "/" + name.replace('.', '/') + ".class";
            File file = new File(filePath);
            byte[] bytes = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);
            fis.read(bytes);
            fis.close();
            return defineClass(name, bytes, 0, bytes.length);
        } catch (IOException e) {
            throw new ClassNotFoundException(name);
        }
    }
}

public class CustomLoaderDemo {
    public static void main(String[] args) throws Exception {
        MyClassLoader loader = new MyClassLoader("E:\\Work\\temp\\ssm-demo\\src\\main\\java\\");

        // 假设 E:/temp/Hello.class 存在
//        Class<?> clazz = loader.loadClass("Hello");
        Class<?> clazz = CustomLoaderDemo.class.getClassLoader().loadClass("Hello");
        System.out.println("类加载器: " + clazz.getClassLoader());

        Object obj = clazz.getDeclaredConstructor().newInstance();
        clazz.getMethod("say").invoke(obj);

    }
}