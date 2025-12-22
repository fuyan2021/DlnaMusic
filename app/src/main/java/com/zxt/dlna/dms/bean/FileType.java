package com.zxt.dlna.dms.bean;

import android.annotation.SuppressLint;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;

//import zidoo.model.BoxModel;

/**
 * 文件类型工具.
 * <p>
// * 为了避免因为型号问题出现判断错误,{@link BoxModel#MODEL_MSTART}型号的{@link BoxModel#MODEL_REALTEK}型号的要先调用{@link #initModelType(int, boolean)}
 *
 * @author lic
 */
@SuppressLint("DefaultLocale")
public abstract class FileType {

	/** 特殊文件ID跨度 */
	private static final int SPECIAL_ID_SKIP = 1000000;
	/** 特殊文件类型跨度 */
	private static final int SPECIAL_TYPE_SKIP = 1000;

	/** 文件夹 */
	public static final int DIR = 0;
	/** 音乐 */
	public static final int MUSIC = 1;
	/** 视频 */
	public static final int MOVIE = 2;
	/** 图片 */
	public static final int PIC = 3;
	/** 文本 */
	public static final int TXT = 4;
	/** 安装包 */
	public static final int APK = 5;
	/** 便携文档 */
	public static final int PDF = 6;
	/** 文档 */
	public static final int WORD = 7;
	/** 表格 */
	public static final int EXCEL = 8;
	/** 演示文稿 */
	public static final int PPT = 9;
	/** 网页 */
	public static final int HTML = 10;
	/** 安装包 */
	public static final int ZIP = 11;
	/** 未知 */
	public static final int OTHER = 12;
	public static final int M3U = 13;
	/** 蓝光文件夹 */
	public static final int BLU_RAY = /*1 * */SPECIAL_ID_SKIP + MOVIE * SPECIAL_TYPE_SKIP + DIR;
	/** DVD文件夹 */
	public static final int DVD = 2 * SPECIAL_ID_SKIP + MOVIE * SPECIAL_TYPE_SKIP + DIR;
	/** 原盘镜像文件 */
	public static final int ISO = 3 * SPECIAL_ID_SKIP + OTHER * SPECIAL_TYPE_SKIP + MOVIE;
	/** 蓝光原盘镜像 */
	public static final int ISO_MOVIE = 4 * SPECIAL_ID_SKIP + MOVIE * SPECIAL_TYPE_SKIP + MOVIE;
	/** 音乐原盘镜像 */
	public static final int ISO_MUSIC = 5 * SPECIAL_ID_SKIP + MUSIC * SPECIAL_TYPE_SKIP + MUSIC;
	/** 音乐列表文本文件 */
	public static final int CUE = 6 * SPECIAL_ID_SKIP + OTHER * SPECIAL_TYPE_SKIP + OTHER;
	/** 有对应音乐文件的音乐列表文本 */
	public static final int CUE_MUSIC = 7 * SPECIAL_ID_SKIP + MUSIC * SPECIAL_TYPE_SKIP + OTHER;
	/** 光碟 */
	public static final int CD_ROM = 8 * SPECIAL_ID_SKIP /*+ DIR * SPECIAL_TYPE_SKIP*/ + DIR;
	/** 蓝光光碟 */
	public static final int CD_ROM_BDMV = 9 * SPECIAL_ID_SKIP + MOVIE * SPECIAL_TYPE_SKIP + DIR;
	/** DVD光碟 */
	public static final int CD_ROM_DVD = 10 * SPECIAL_ID_SKIP + MOVIE * SPECIAL_TYPE_SKIP + DIR;

	/** UPNP */
	public static final int UPNP = 63;
	/** UPNP文件夹 */
	public static final int UPNP_DIR = 631 * SPECIAL_ID_SKIP /*+ FILE_TYPE_DIR * SPECIAL_TYPE_SKIP + FILE_TYPE_DIR*/;
	/** UPNP音乐 */
	public static final int UPNP_AUDIO = 632 * SPECIAL_ID_SKIP + MUSIC * SPECIAL_TYPE_SKIP + MUSIC;
	/** UPNP视频 */
	public static final int UPNP_VIDEO = 633 * SPECIAL_ID_SKIP + MOVIE * SPECIAL_TYPE_SKIP + MOVIE;
	/** UPNP图像 */
	public static final int UPNP_IMAGE = 634 * SPECIAL_ID_SKIP + PIC * SPECIAL_TYPE_SKIP + PIC;
	/** UPNP其它文件 */
	public static final int UPNP_OTHER = 635 * SPECIAL_ID_SKIP + OTHER * SPECIAL_TYPE_SKIP + OTHER;

	// public static boolean sIsXtreamer = false;

	private static final ArrayList<FileType> types = new ArrayList<>();

	private final int type;

	public FileType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	abstract boolean isType(String name, String ext);

	/**
	 * @param model 型号
	 * @param isXtreamer 盒子型号是否是Xtreamer
	 */
//	public static void initModelType(int model, boolean isXtreamer) {
//
//		if (model == BoxModel.MODEL_MSTART && isXtreamer) {
//			FileType dbsMusic = new FileType(MUSIC) {
//				@Override
//				boolean isType(String name, String ext) {
//					return ext.equalsIgnoreCase("dbs") && isAudioFile(name.substring(0, name.length() - ext.length() - 1));
//				}
//			};
//
//			FileType dbsMovie = new FileType(MOVIE) {
//
//				@Override
//				boolean isType(String name, String ext) {
//					return ext.equalsIgnoreCase("dbs") && isMovieFile(name.substring(0, name.length() - ext.length() - 1));
//				}
//			};
//
//			types.add(dbsMusic);
//			types.add(dbsMovie);
//		} else if (model == BoxModel.MODEL_REALTEK) {
//			types.add(new SingleType(MOVIE, "vp9"));
//		}
//	}

	/**
	 * 恢复默认设置
	 */
	public static void reset() {
		types.clear();
		// 音乐
		String[] musics = new String[]{"aac", "aif", "aiff", "amr", "ape", "dts", "dsf", "dff", "flac", "m4a", "m4p", "m4r", "mid", "midi",
			"mp1",
			"mp2", "mp3", "mp5", "mpa", "mpga", "ogg", "wav", "wma", "mmf", "wv", "nrg"};
		types.add(new ArrayType(MUSIC, musics));
		// 视频
		String[] movies = new String[]{"3dm", "3dv", "3g2", "3gp", "3gpp", "3gpp2", "asf", "avi", "dat", "divx", "f4v", "flv", "lge",
			"m2t", "m2ts", "m4b", "m4p", "m4v", "mkv", "mov", "mp4", "mpeg", "mpg", "pmp", "psr", "ram", "rm", "rmvb", "tp", "ts", "vob",
			"webm", "wmv", "mts", "m3u8"};
		types.add(new ArrayType(MOVIE, movies));
		// 图片
		String[] pictures = new String[]{"bmp", "gif", "jfif", "jpeg", "jpg", "png", "tiff"};
		types.add(new ArrayType(PIC, pictures));
		// 文本
		String[] texts = new String[]{"epub", "fb2", "pdb", "rtf", "txt"};
		types.add(new ArrayType(TXT, texts));
		// 安装包
		types.add(new SingleType(APK, "apk"));
		// 便携文档
		types.add(new SingleType(PDF, "pdf"));
		// 文档
		types.add(new ArrayType(WORD, new String[]{"doc", "docx"}));
		// 表格
		types.add(new ArrayType(EXCEL, new String[]{"xls", "xlsx"}));
		// 演示文稿
		types.add(new ArrayType(PPT, new String[]{"ppt", "pptx"}));
		// 网页
		types.add(new SingleType(HTML, "html"));
		// 压缩包
		types.add(new ArrayType(ZIP, new String[]{"zip", "rar"}));
		// 原盘镜像
		types.add(new SingleType(ISO, "iso"));
		// 音乐列表文本
		types.add(new SingleType(CUE, "cue"));
		// m3u播放列表
		types.add(new SingleType(M3U, "m3u"));
	}

	/**
	 * 注册自定义文件类型
	 *
	 * @param type 类型[1-31]
	 * @param exts 后缀名列表
	 */
	public static void registerFileType(int type, String[] exts) {
		types.add(0, new ArrayType(type, exts));
	}

	/**
	 * 获取类型
	 *
	 * @param file 文件或目录
	 * @return 如果是目录, 返回{@link #DIR},否则返回与文件相对应的类型
	 */
	public static int getType(File file) {
		if (file.isDirectory()) {
			return FileType.DIR;
		}
		return getTypeX(file) % SPECIAL_TYPE_SKIP;
	}


	/**
	 * 获取文件类型，在{@link #getType(File)}的基础上，增加了蓝光文件夹，蓝光导航，CUE三个类型
	 */
	public static int getTypeX(File file) {
		String path = file.getPath();
		if (path.matches("/dev/block/sr\\d+")) {
			File mountFile = new File(path.replace("/dev/block/sr", "/storage/CDROOM"));
			if (FileType.isBDMV(mountFile.getPath())) {
				return CD_ROM_BDMV;
			} else if (FileType.isDvd(mountFile.getPath())) {
				return CD_ROM_DVD;
			} else {
				return CD_ROM;
			}
		} else if (file.isDirectory()) {
			if (isBDMV(path)) {
				return BLU_RAY;
			} else if (isDvd(path)) {
				return DVD;
			} else {
				return DIR;
			}
		} else {
			int type = getFileTypeX(file.getName());
			switch (type) {
				case ISO:
					if (isIsoMusic(path)) {
						return ISO_MUSIC;
					} else {
						return ISO_MOVIE;
					}
				case CUE:
					if (findCueFile(path) != null) {
						return CUE_MUSIC;
					}
					break;
				default:
					break;
			}
			return type;
		}
	}

	/**
	 * 获取普通文件的类型
	 *
	 * @param name 文件名
	 * @return 与文件对应的类型
	 */
	public static int getFileType(String name) {
		return getFileTypeX(name) % SPECIAL_TYPE_SKIP;
	}

	public static int getFileTypeX(String name) {
		int p = name.lastIndexOf(".");
		if (p != -1) {
			String ext = name.substring(p + 1).toLowerCase();
			return getFileType(name, ext);
		}
		return OTHER;
	}

	private static int getFileType(String name, String ext) {
		for (FileType type : types) {
			if (type.isType(name, ext)) {
				return type.getType();
			}
		}
		return OTHER;
	}

	/**
	 * 普通文件是否是特定的类型
	 *
	 * @param type 特定的类型
	 * @param fileName 文件名
	 * @return 如果文件类型是特定的类型, 返回真
	 */
	public static boolean isType(int type, String fileName) {
		int p = fileName.lastIndexOf(".");
		if (p == -1) {
			return type == OTHER;
		}
		String ext = fileName.substring(p + 1).toLowerCase();
		if (type == OTHER) {
			return getFileType(fileName, ext) == OTHER;
		} else {
			for (FileType fileType : types) {
				if (fileType.getType() == type && fileType.isType(fileName, ext)) {
					return true;
				}
			}
		}
		return false;
	}

	public static int toType(int typeX) {
		return typeX % SPECIAL_TYPE_SKIP;
	}

	/**
	 * 是否是视频文件(非文件夹)
	 *
	 * @return 如果是视频文件, 返回真, 否则返回假
	 */
	public static boolean isMovieFile(String fileName) {
		return isType(MOVIE, fileName) || isType(ISO, fileName);
	}

	public static boolean isMovieFile(File file) {
		return isIsoMovie(file.getAbsolutePath()) || isType(MOVIE, file.getName());
	}

	/**
	 * 是否是视频（包括文件夹形式的视频）
	 *
	 * @param file 文件/文件夹
	 */
	public static boolean isVideo(File file) {
		int typeX = getTypeX(file);
		return typeX == MOVIE || typeX / 1000 % 1000 == MOVIE;
	}

	/**
	 * 是否是音乐文件
	 *
	 * @return 如果是音乐文件, 返回真, 否则返回假
	 */
	public static boolean isAudioFile(String fileName) {
		return isType(MUSIC, fileName);
	}

	public static boolean isAudioFile(File file) {
		return isType(MUSIC, file.getName()) || isDsfMusic(file.getAbsolutePath());
	}
	//jiangbo

	/**
	 * 是否是DSF
	 *
	 * @author jiangbo
	 * Nov 18, 2017
	 */
	public static boolean isDsfMusic(String path) {
		String ext = getFileExt(path).toLowerCase();
		if (ext.equals("iso")) {
			return isIsoMusic(path);
		} else {
			return ext.equals("dsf") || ext.equals("dff");
		}
	}

	public static boolean isIsoMovie(String path) {
		String ext = getFileExt(path).toLowerCase();
		return ext.equals("iso") && !isIsoMusic(path);
	}

	private static boolean isIsoMusic(String path) {
		try {
			File file = new File(path);
			if (!file.exists()) {
				return false;
			}
			if(path.toLowerCase().contains("cloudnas")||path.toLowerCase().contains("clouddrive")){
				if(file.length() >= 5L * 1024 * 1024 * 1024){
					return false;
				}else {
					return true;
				}
			}
			int START_OF_MASTER_TOC = 510;
			int SACD_LSN_SIZE = 2048;
			{
				FileInputStream in = new FileInputStream(file);
				byte buffer[] = new byte[8];
				in.skip(START_OF_MASTER_TOC * SACD_LSN_SIZE);
				in.read(buffer);
				in.close();
				String rString = new String(buffer);
//				Log.v("bob", "1 rString = " + rString);
				if (rString.equals("SACDMTOC")) {
					return true;
				}
			}
			{
				int SACD_PSN_SIZE = 2064;
				FileInputStream in = new FileInputStream(file);
				byte buffer[] = new byte[8];
				in.skip(START_OF_MASTER_TOC * SACD_PSN_SIZE + 12);
				in.read(buffer);
				in.close();
				String rString = new String(buffer);
//				Log.v("bob", "2 rString = " + rString);
				if (rString.equals("SACDMTOC")) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	//jiangbo

	/**
	 * 是否是图片文件
	 *
	 * @return 如果是图片文件, 返回真, 否则返回假
	 */
	public static boolean isPictureFile(String fileName) {
		return isType(PIC, fileName);
	}

	/**
	 * 是否是文本文件
	 *
	 * @return 如果是文本文件, 返回真, 否则返回假
	 */
	public static boolean isTxtFile(String fileName) {
		return isType(TXT, fileName);
	}

	/**
	 * 是否是应用程序安装包
	 *
	 * @return 如果是安装包, 返回真, 否则返回假
	 */
	public static boolean isApkFile(String fileName) {
		return getFileExt(fileName).toLowerCase().equals("apk");
	}

	/**
	 * 是否是pdf文件
	 *
	 * @return 如果是pdf文件, 返回真, 否则返回假
	 */
	public static boolean isPdfFile(String fileName) {
		return getFileExt(fileName).toLowerCase().equals("pdf");
	}

	/**
	 * 是否是word文件
	 *
	 * @return 如果是word文件, 返回真, 否则返回假
	 */
	public static boolean isWordFile(String fileName) {
		return isType(WORD, fileName);
	}

	/**
	 * 是否是Excel文件
	 *
	 * @return 如果是Excel文件, 返回真, 否则返回假
	 */
	public static boolean isExcelFile(String fileName) {
		return isType(EXCEL, fileName);
	}

	/**
	 * 是否是PPT文件
	 *
	 * @return 如果是PPT文件, 返回真, 否则返回假
	 */
	public static boolean isPptFile(String fileName) {
		return isType(PPT, fileName);
	}

	/**
	 * 是否是html文件
	 *
	 * @return 如果是html文件, 返回真, 否则返回假
	 */
	public static boolean isHtmlFile(String fileName) {
		return getFileExt(fileName).toLowerCase().equals("html");
	}

	/**
	 * 是否是Zip文件
	 *
	 * @return 如果是Zip文件, 返回真, 否则返回假
	 */
	public static boolean isZipFile(String fileName) {
		return isType(ZIP, fileName);
	}

	/**
	 * 是否是蓝光文件夹
	 *
	 * @param path 文件夹路径
	 * @return 如果是蓝光文件夹, 返回真, 否则返回假
	 */
	public static boolean isBDMV(String path) {
		File bdmv = new File(path + "/BDMV/STREAM");
		return bdmv.exists() && bdmv.isDirectory();
	}

	/**
	 * 判断文件路径是否是DvD文件夹
	 */
	public static boolean isDvd(String path) {
//		File vts = new File(path, "VIDEO_TS.IFO");
//		if (vts.exists()) {
//			return true;
//		}
		File vtss = new File(path, "VIDEO_TS/VIDEO_TS.IFO");
		return vtss.exists();
	}

	/**
	 * 是否量CUE文件
	 *
	 * @param path 文件路径
	 */
	public static String isCUE(String path) {
		if (!getFileExt(path).toLowerCase().equals("cue")) {
			return null;
		}
		File cue = findCueFile(path);
		if (cue != null) {
			return cue.getPath();
		}
		return null;
	}

	public static boolean isUpnpFile(File file) {
		return isUpnp(getTypeX(file));
	}

	public static boolean isUpnp(int typeX) {
		return typeX / SPECIAL_ID_SKIP / 10 == UPNP;
	}


	public static File findCueFile(String path) {
		String cueExtArray[] = {"ape", "APE", "wav", "WAV", "flac", "FLAC", "dts", "DTS"};
		String pre = path.substring(0, path.length() - 3);
		for (String cue : cueExtArray) {
			File file = new File(pre + cue);
			if (file.exists()) {
				return file;
			}
		}
		return null;
	}

	public static String getFileExt(String name) {
		int p = name.lastIndexOf('.');
		return p == -1 ? "" : name.substring(p + 1);
	}

	public static final class SingleType extends FileType {

		private String ext;

		public SingleType(int type, String ext) {
			super(type);
			this.ext = ext;
		}

		@Override
		boolean isType(String name, String ext) {
			return this.ext.equals(ext);
		}

	}

	public static class ArrayType extends FileType {

		private HashSet<String> names = new HashSet<String>();

		public ArrayType(int type, String[] exts) {
			super(type);

			for (String name : exts) {
				names.add(name);
			}
		}

		public void addExt(String ext) {
			names.add(ext);
		}

		public boolean remove(String ext) {
			return names.remove(ext);
		}

		@Override
		boolean isType(String name, String ext) {
			return names.contains(ext);
		}
	}

	static {
		reset();
	}

}
