package cn.com.zcha.zip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;

import org.apache.log4j.Logger;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

/**
 * 文件进行ZIP压缩和解压缩的工具类
 * 数据压缩和解压缩的工具类
 * @version 1.0
 * @date 2015/05/08
 * @author jhd
 *
 */
public class ZipUtil {
	  private static Logger log = Logger.getLogger(ZipUtil.class);
	  static final int BUFFER = 2048;
	  static final byte[] ByteBuff = new byte[2048];
	  static int pathIndex = 1;  ////用来标识是文件还是文件夹
	  /**
	   * 默认将压缩后的文件放在同一路径下
	   * @param sourceFilePath 路径的格式  D:\\XX\\YY 或者 D:\\XX\\YY.
	   * @throws Exception 
	   */
	  public static void zip(String sourceFilePath) throws Exception{
		  pathIndex = 1;
		  String DestFileName = "";
		  sourceFilePath = sourceFilePath.trim();
		    
		    File TempFile = new File(sourceFilePath);
		    if (!TempFile.exists())
		    {
		      throw new Exception("文件不存在");
		    }
		    if (TempFile.isDirectory()) /////文件夹
		    {
		    	if ((sourceFilePath.substring(sourceFilePath.length() - 1) .equals("\\") ) 
		    		|| (sourceFilePath.substring(sourceFilePath.length() - 1) .equals("/") )){
		    		
		    		sourceFilePath = sourceFilePath.substring(0, sourceFilePath.length() - 1);
		   	     }
		    	sourceFilePath = sourceFilePath.replace('/', '\\');
		   	    DestFileName = TempFile.getParent();
		    }else{
		    	/////默认和该文件同名
		    	DestFileName = TempFile.getParent()+"\\"+TempFile.getName()+".zip";
		    }
		    zip(sourceFilePath, DestFileName);
	  }
	  
	  
	  /**
	   * 同步压缩方法
	   * @param sourceFilePath 路径格式  D:\\XX\\YY 或者 D:\\XX\\YY.
	   * @param destFilePath  目标路径格式 D:\\XX\\YY 或者 D:\\xx\\YY.zip
	   * @throws Exception
	   */
	  public static synchronized void zip(String sourceFilePath,String destFilePath) throws Exception{
		  pathIndex = 1;
		  log.info("线程名称："+Thread.currentThread().getName());
		  File SourceFile = new File(sourceFilePath);
		  	if (!SourceFile.exists()){
		    	log.info("不存在");
		    	throw new Exception("文件不存在");
		   }
		  	if(destFilePath.length()>4){
		    	 if (!destFilePath.substring(destFilePath.length() - 4).toLowerCase().equals(".zip")){ /////文件以.zip结束
		 	    	String name = SourceFile.getName();
		 	    	destFilePath = destFilePath+"\\"+name+".zip";
		 	    }
		    }else{
		    	String name = SourceFile.getName();
		    	destFilePath = destFilePath+"\\"+name+".zip";
		    }

		  	destFilePath = destFilePath.replace('\\', '/');
		    if (destFilePath.indexOf("/") != -1){
		      String DirPath = destFilePath.substring(0, destFilePath.indexOf("/") + 1);
		      File TempFile = new File(DirPath);
		      if (!TempFile.exists()){
		    	  log.info("目标路径不存在");
		    	  throw new Exception("目标路径不存在");
		      }
		      if (!TempFile.isDirectory()){
		    	  log.info("目标路径不是文件夹");
		    	  throw new Exception("");
		      }
		      File destFile = new File(destFilePath);
		      log.info("源文件："+sourceFilePath+" 目标文件："+destFilePath);
		      while(destFile.exists()){//////判断文件是否存在，如果存在则递增
		    	  int temp = 0;
		    	  String count = destFile.getName().substring(destFile.getName().indexOf(".zip")-1,destFile.getName().indexOf(".zip"));
		    	  int cc = count.charAt(0);
		    	  if(cc >= 48 && cc <= 57){
		    		  temp = Integer.valueOf(count)+1;
		    		  destFilePath = destFile.getParent()+"\\"+destFile.getName().substring(0, destFile.getName().indexOf(".zip")-1)+temp+".zip";
		    	  }else{
		    		  destFilePath = destFile.getParent()+"\\"+destFile.getName().replace(".zip", "")+temp+".zip";
		    	  }
		    	  destFile = new File(destFilePath);
		      }
		    }
		   
		  FileOutputStream fos = new FileOutputStream(destFilePath);
//		  
		  BufferedOutputStream bos = new BufferedOutputStream(fos);
		  CheckedOutputStream cos = new CheckedOutputStream(bos,new CRC32());
		  ZipOutputStream zos = new ZipOutputStream(cos);
		  zip(zos,sourceFilePath,"");
		  zos.closeEntry();
		  log.info("压缩成功");
		  if(zos != null){
			  zos.close();
		  }
	  }
	  
	  /**
	   * 如果路径指向的是文件夹，则遍历；如果不是，则直接压缩
	   * @param destFilePath 
	   * @param sourceFilePath
	   * @param relativePath
	   * @throws Exception
	   */
	  private static void zip(ZipOutputStream zos,String sourceFilePath,String relativePath) throws Exception{
		  
		  File SourceFile = new File(sourceFilePath);
		  try{
		      if(SourceFile.isDirectory()){
		  	       if((sourceFilePath.endsWith("\\") ) || (sourceFilePath.endsWith("/") )){
		  	        	sourceFilePath = sourceFilePath.substring(0, sourceFilePath.length() - 1);
		  	        }
		  	       sourceFilePath = sourceFilePath.replace('/', '\\');
		  	       
		  	       /////获取所有的文件
		  	       File[] filess = SourceFile.listFiles(); 
		  	       for(int i = 0; i < filess.length; i++){ ////循环遍历
		  	    	   File tempFile = filess[i];
		  	    	   if (!tempFile.isDirectory()){ /////该路径指向的是文件
		  	        		zip(zos,tempFile,relativePath+SourceFile.getName()+"\\");
		  	        		log.info(tempFile.getName()+"  压缩成功  ");
		  	    	   }else{
		  	    		   if( pathIndex == 1){
		  	    			   pathIndex ++;
		  	    			 zip(zos,tempFile.getPath(),relativePath+SourceFile.getName()+"\\");////relativePath+tempFile.getName()+"\\"
		  	    		   }else{
		  	    			 zip(zos,tempFile.getPath(),relativePath+tempFile.getName()+"\\");
		  	    		   }
		  	    	   }
		  	       	}
		      }
		      else{
		    	  String fileName = SourceFile.getName();
		    	  File myFile = new File(sourceFilePath.substring(0,sourceFilePath.lastIndexOf("\\")+1)+fileName);
		    	  zip(zos,myFile,relativePath);
		    	  log.info(myFile.getName()+" 压缩成功");
		      }
		      zos.flush();
		    }catch (Exception e){
		    	e.printStackTrace();
		    	log.error("压缩失败");
		    	throw new Exception("压缩文件失败");
		    }
	  }
	  
	  
	  
	  /**
	   *真正的压缩方法
	   * @param zos  压缩输出流
	   * @param file  文件
	   * @param relativePath  相对路径
	   * @throws Exception
	   */
	  private static void zip(ZipOutputStream zos,File file,String relativePath)throws Exception{
		  ZipEntry entry = new ZipEntry(relativePath+file.getName());
		  zos.putNextEntry(entry);
		  FileInputStream fis = null;
		  fis = new FileInputStream(file);
		  BufferedInputStream bis = new BufferedInputStream(fis);
		  CheckedInputStream cis = new CheckedInputStream(bis,new CRC32());
//		  ZipInputStream zis = new ZipInputStream(new BufferedInputStream(cis));
		  int count ;
		  while((count = cis.read(ByteBuff))>=0){
			  zos.write(ByteBuff, 0, count);
		  }
//		  log.info(entry.getName()+" CRC32校验："+cis.getChecksum().getValue());
		  zos.flush();
		  zos.closeEntry();
		  fis.close();
	  }
	  
	  
	  
	  /**
	   * 数据压缩
	   * @param data  需要压缩的数据
	   * @param level 
	   * @return
	   * @throws Exception
	   */
	  public static byte[] compress(byte[] data,int compressionLevel) throws Exception{
		  
		  if(null == data){
			  log.info("数据为空");
			  return data;
		  }
		  log.info("before compress : " + (data.length / 1024) + " k");
          Deflater deflater = new Deflater();
          //设置压缩的等级
          deflater.setLevel(compressionLevel);
          deflater.setInput(data);
  
          ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
          CheckedOutputStream cos = new CheckedOutputStream(outputStream,new CRC32());
          deflater.finish();
          byte[] buffer = new byte[BUFFER];
          while (!deflater.finished()) {
              int count = deflater.deflate(buffer); 
              cos.write(buffer, 0, count); ///outputStream
          }
//          log.info("comp--" +cos.getChecksum().getValue());
          byte[] output = outputStream.toByteArray();
  		  outputStream.close();
  		  log.info("压缩成功,after compress "+(output.length / 1024) + " k");
          return output;
	  }
	  
	  
	  /**
	   * 解压缩数据
	   * @param data
	   * @return
	   * @throws Exception
	   */
	  public static byte[] decompress(byte[] data)throws Exception{
		  Inflater inflater = new Inflater();
          inflater.setInput(data);
          log.info("before decompress :" + (data.length) / 1024 + " k");
          ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
          CheckedOutputStream cos = new CheckedOutputStream(outputStream,new CRC32());
          byte[] buffer = new byte[BUFFER];
          while (!inflater.finished()) {
              int count = inflater.inflate(buffer);
              cos.write(buffer, 0, count); ///outputStream
          }
//          log.info("decomp--" +cos.getChecksum().getValue());
          byte[] output = outputStream.toByteArray();
  		  outputStream.close();
  		  log.info("压缩成功,after decompress "+(output.length / 1024) + " k");
          return output;
	  }
	  
	  
	  /**
	   * 解压缩，默认解压缩后在同一个文件夹下
	   * @param sourceFilePath
	   * @throws Exception
	   */
	  public static void unZip(String sourceFilePath) throws Exception{
		  File source = new File(sourceFilePath);
		  if(!source.exists()){
			  log.info("文件不存在");
			  return ;
		  }
		  String destPath = source.getParent();
		  unZip(sourceFilePath,destPath);
	  }
	  
	  /**
	   * 解压缩
	   * @param sourceFilePath 源文件路径格式  D:\\xx\\YY.zip
	   * @param destFilePath  目标文件 格式  D:\\XX\\YY 
	   * @throws Exception 
	   */
	  @SuppressWarnings("unchecked")
	  public static synchronized void unZip(String sourceFilePath,String destFilePath) throws Exception{
		    CheckedInputStream cis = null;
		    File tempFile = new File(sourceFilePath);
		    
		    if(!tempFile.exists()){
		      log.info("文件不存在");
		      throw new Exception("文件不存在");
		    }
		    
		    if(tempFile.isDirectory()){
		      log.info("源文件是文件夹");
		      throw new Exception("源文件是文件夹");
		    }

		    tempFile = new File(destFilePath);
		    if(!tempFile.exists()){
		      try{
		    	  tempFile.mkdir();
		      }catch (Exception ex) {
		    	  log.info("目标路径不存在");
		    	  throw new Exception("目标路径不存在");
		      }
		    }

		    if(!tempFile.isDirectory()){
		      log.error("目标路径不是文件夹");
		      throw new Exception("目标路径不是文件夹");
		    }
		    if((!destFilePath.endsWith("\\")) && (destFilePath.endsWith("/"))){
		    	destFilePath = destFilePath + "\\";
		    }
		    destFilePath = destFilePath.replace('/', '\\');
		    try
		    {
		    	OutputStream  os = null;
		    	InputStream is = null;
		    	ZipFile zipFile = new ZipFile(sourceFilePath);
		    	
				Enumeration<ZipEntry> e = zipFile.getEntries();
		    	ZipEntry zipEntry = null;
		    	while (e.hasMoreElements()){
		    		String curPath = destFilePath;
		    		zipEntry = (ZipEntry)e.nextElement();
		    		String fileName = zipEntry.getName();
		    		fileName = fileName.replace('\\', '/');
		    		if(zipEntry.getSize()>0){ ////
		    			if(fileName.indexOf("/") != -1){
		    				curPath = curPath+"\\"+fileName.substring(0, fileName.lastIndexOf("/"));
		    				File newFile = new File(curPath);
		    				if(!newFile.exists()){////不存在，则创建
		    					newFile.mkdir();
		    				}
		    			}
		    			File outFile = new File(destFilePath+"\\"+zipEntry.getName());
		    			FileOutputStream fos = new FileOutputStream(outFile);
		    			os = new BufferedOutputStream(fos);
		    			is = zipFile.getInputStream(zipEntry);
		    			cis = new CheckedInputStream(is,new CRC32());
		    			int count ;
		    			while( (count= cis.read(ByteBuff))>=0){
		    				os.write(ByteBuff, 0, count); 
		    			}
//		    			log.info(outFile.getName()+" CRC32校验："+cis.getChecksum().getValue());
		    			cis.close();
		    			os.close();
		    			is.close();
		    		}
		    		
		    	}
		    	zipFile.close();
		    	log.info("解压缩成功");
		    }catch (Exception e){
		      e.printStackTrace();
		      log.error("解压缩失败...");
		      throw new Exception("解压缩失败");
		    }
	  }
	  
	  
	  public static void main(String[] args) {
		try {
			
//			ZipUtil.zip("D:\\360Downloads\\pic", "D:\\360downloadsss\\");
//			unZip("D:\\360downloadsss\\HotFix.zip");
//			MyThread mt = new MyThread();
//			MyThread mt2 = new MyThread();
//			for(int i = 0; i <3; i ++){
//				Thread t = new Thread(mt,"name of "+i);
//				Thread t2 = new Thread(mt2,"name of mt2 :"+i);
//				t2.start();
//				t.start();
//			}
			
			
//			BufferedInputStream in = new BufferedInputStream(new FileInputStream(
//	                "E:\\workspace\\ZipUtils\\src\\test.properties"));
//	        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
//	        byte[] temp = new byte[1024];
//	        int size = 0;
//	        while ((size = in.read(temp)) != -1) {
//	            out.write(temp, 0, size);
//	        }
//	        in.close();
//	        byte[] data = out.toByteArray();
//	        byte[] output = ZipUtil.compress(data,Deflater.BEST_COMPRESSION);
//
//	        FileOutputStream fos = new FileOutputStream("E:\\workspace\\ZipUtils\\src\\out_put.txt.bak.compress");
//	        fos.write(output);
//	        out.close();
//	        fos.close();
			
			
			///////////
//			
			 	BufferedInputStream in = new BufferedInputStream(new FileInputStream(
		                "E:\\workspace\\ZipUtils\\src\\out_put.txt.bak.compress"));
		        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
		        byte[] temp = new byte[1024];
		        int size = 0;
		        while ((size = in.read(temp)) != -1) {
		            out.write(temp, 0, size);
		        }
		        in.close();
		        byte[] data = out.toByteArray();
		        byte[] output = ZipUtil.decompress(data);

		        FileOutputStream fos = new FileOutputStream("E:\\workspace\\ZipUtils\\src\\out_put.properties");
		        fos.write(output);
		        out.close();
		        fos.close();
//			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

//
//class MyThread implements Runnable{
//
//	@Override
//	public void run() {
//		try {
//			ZipUtil.zip("D:\\360Downloads\\HotFix","D:\\360downloadsss\\");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//}

