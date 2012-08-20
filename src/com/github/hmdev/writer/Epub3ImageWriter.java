package com.github.hmdev.writer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.velocity.app.Velocity;

import com.github.hmdev.converter.AozoraEpub3Converter;
import com.github.hmdev.info.SectionInfo;

/** ePub3用のファイル一式をZipで固めたファイルを生成.
 * 画像のみのZipの場合こちらで画像専用の処理を行う
 */
public class Epub3ImageWriter extends Epub3Writer
{
	/** コピーのみのファイル */
	final static String[] TEMPLATE_FILE_NAMES = new String[]{
		"mimetype",
		"META-INF/container.xml",
		OPS_PATH+CSS_PATH+"vertical_text.css",
		OPS_PATH+CSS_PATH+"vertical_middle.css",
		OPS_PATH+CSS_PATH+"vertical_image.css",
		OPS_PATH+CSS_PATH+"vertical.css",
		OPS_PATH+CSS_PATH+"horizontal_text.css",
		OPS_PATH+CSS_PATH+"horizontal_middle.css",
		OPS_PATH+CSS_PATH+"horizontal_image.css",
		OPS_PATH+CSS_PATH+"horizontal.css"
	};
	
	/** 出力先ePubのZipストリーム */
	ZipArchiveOutputStream zos;
	
	/** コンストラクタ
	 * @param templatePath epubテンプレート格納パス文字列 最後は"/"
	 */
	public Epub3ImageWriter(String templatePath)
	{
		super(templatePath);
	}
	
	/** 本文を出力する */
	@Override
	void writeSections(AozoraEpub3Converter converter, BufferedReader src, BufferedWriter bw, HashSet<String> zipImageFileNames) throws IOException
	{
		//ファイルをソート
		String[] fileNames = new String[zipImageFileNames.size()];
		zipImageFileNames.toArray(fileNames);
		Arrays.sort(fileNames);
		
		//画像xhtmlを出力
		for (String srcFilePath : fileNames) {
			this.startImageSection();
			String fileName = this.getImageFilePath(srcFilePath.trim());
			bw.write(converter.getChukiValue("画像開始")[0]);
			bw.write(fileName);
			bw.write(converter.getChukiValue("画像終了")[0]);
			bw.flush();
		}
	}
	
	/** セクション開始. 
	 * @throws IOException */
	private void startImageSection() throws IOException
	{
		this.sectionIndex++;
		String sectionId = decimalFormat.format(this.sectionIndex);
		//package.opf用にファイル名
		SectionInfo sectionInfo = new SectionInfo(sectionId);
		//画像専用指定
		sectionInfo.setImageFit(true);
		this.sectionInfos.add(sectionInfo);
		this.addChapter(sectionId, ""+this.sectionIndex); //章の名称はsectionIdを仮に設定
		super.zos.putArchiveEntry(new ZipArchiveEntry(OPS_PATH+XHTML_PATH+sectionId+".xhtml"));
		//ヘッダ出力
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(super.zos, "UTF-8"));
		//出力開始するセクションに対応したSectionInfoを設定
		this.velocityContext.put("sectionInfo", sectionInfo);
		Velocity.getTemplate(this.templatePath+OPS_PATH+XHTML_PATH+XHTML_HEADER_VM).merge(this.velocityContext, bw);
		bw.flush();
	}
}
