
package com.extract.service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;

import util.IOUtils;
import util.db.DbMysqlUtil;



public class ExtractCSV
{
	private LineNumberReader dataReadIn;
	private ExecutorService dataExtractPool;

	public ExtractCSV(String uploadFolder, int uploadThread) throws IOException
	{
		File[] uploadFiles = new File(uploadFolder).listFiles();
		for ( File _tmpFile : uploadFiles )
		{
			this.dataReadIn = new LineNumberReader(new FileReader(_tmpFile.getPath()));
		}
		if ( uploadFiles != null || uploadThread != 0 )
			this.dataExtractPool = Executors.newFixedThreadPool(uploadThread);
	}

	public static void main(String[] args) throws InterruptedException, IOException, ConfigurationException
	{
		ExtractCSV ReadExcel = new ExtractCSV(LoadConfig.sourceFolder, LoadConfig.extractThread);
		ReadExcel.start();
	}

	private void start() throws InterruptedException
	{
		if ( dataExtractPool != null )
		{
			new DataExtractThread().start();
		}
	}

	private class DataExtractThread extends Thread
	{
		@SuppressWarnings("unused")
		private void dataExtractRunner() throws InterruptedException
		{
			try
			{
				dataExtractPool.execute(null);
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}

		public void run()
		{
			try
			{
				ArrayList<Object[]> arrayData = new ArrayList<Object[]>();
				String[] columeList = LoadConfig.columnCsvList;
				for ( String line = dataReadIn.readLine(); line != null; line = dataReadIn.readLine() )
				{
					if ( dataReadIn.getLineNumber() > LoadConfig.skipLine )
					{
						String[] strs = line.split(",");
						System.out.println(dataReadIn.getLineNumber() + ", " + strs.length);
						Object[] oValue = new Object[columeList.length];
						for ( int i = 0; i < columeList.length; i++ )
						{
							oValue[i] = putInsertValue(strs, i);
						}
						arrayData.add(oValue);

					}
				}

				StringBuilder sbKey = new StringBuilder();
				StringBuilder sbValue = new StringBuilder();
				for ( int i = 0; i < columeList.length; i++ )
				{
					sbKey.append(columeList[i] + ",");
					sbValue.append("?,");
				}
				sbKey.deleteCharAt(sbKey.length() - 1);
				sbValue.deleteCharAt(sbValue.length() - 1);

				// insert key
				StringBuffer stSQL = new StringBuffer();
				stSQL.append(" INSERT INTO " + LoadConfig.tableName);
				stSQL.append(" ( " + sbKey + " )");
				stSQL.append(" VALUES (" + sbValue + " ) ");
				System.out.println(stSQL.toString());
				System.out.println(arrayData.size());
				DbMysqlUtil aaa = new DbMysqlUtil(LoadConfig.conf);
				aaa.getConnection();
				aaa.doInsert(stSQL.toString(), arrayData);
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
			finally
			{
				IOUtils.closeQuietly(dataReadIn);
				dataExtractPool.shutdown();
				try
				{
					dataExtractPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MICROSECONDS);
				}
				catch ( InterruptedException e )
				{
					e.printStackTrace();
				}
			}
		}

		private String putInsertValue(String[] strs, int position)
		{
			if ( strs.length <= ( position ) )
				return StringUtils.EMPTY;
			return ( strs[( position )] == null )? StringUtils.EMPTY: strs[( position )];
		}
	}
}
