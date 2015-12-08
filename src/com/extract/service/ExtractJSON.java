
package com.extract.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import util.db.DbMysqlUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ExtractJSON
{
	public static void main(String[] args) throws IOException, SQLException
	{
		//subString
		Map<String, Integer> subStringMap = new HashMap<String, Integer>();
		String[] subStringList = LoadConfig.subStringJsonList;
		for ( String _s : subStringList )
		{
			String[] _m = _s.split(">");
			subStringMap.put(_m[0].trim(), Integer.parseInt(_m[1].trim()));
		}
			
		// columns
		Map<String, String> columnValue = new HashMap<String, String>();
		Map<String, String> valueMap = new HashMap<String, String>();
		Set<String> sqlValue = new HashSet<String>();
		String[] columeList = LoadConfig.columnJsonList;
		for ( String _c : columeList )
		{
			String[] _m = _c.split(">");
			valueMap.put(_m[1].trim(), _m[0].trim());
			sqlValue.add(_m[0].trim());
		}

		JsonArray json = readJsonFromUrl(LoadConfig.sourceURL);
		ArrayList<Object[]> arrayData = new ArrayList<Object[]>();
		for ( int i = 0; i < json.size(); i++ )
		{
			columnValue = new HashMap<String, String>();
			Iterator<JsonElement> keys = ( (JsonArray)json.get(i) ).iterator();
			while ( keys.hasNext() )
			{
				Iterator keyIter = ( (JsonObject)keys.next() ).entrySet().iterator();
				while ( keyIter.hasNext() )
				{
					Entry entry = (Map.Entry)keyIter.next();
					String key = entry.getKey().toString().trim().replaceAll("\"", "");
					String value = entry.getValue().toString().trim().replaceAll("\"", "");
					if ( valueMap.get(key) != null )
					{
						if ( columnValue.get(valueMap.get(key)) != null )
						{
							String _sb = columnValue.get(valueMap.get(key)) + "\n";
							columnValue.put(valueMap.get(key), _sb + value);
						}
						else
						{
							if(subStringMap.containsKey(valueMap.get(key)) && value.length()> subStringMap.get(valueMap.get(key)))
								value = value.substring(0, subStringMap.get(valueMap.get(key)));
							
							columnValue.put(valueMap.get(key), value);
						}
					}
				}
			}

			int k = 0;
			Object[] oValue = new Object[sqlValue.size()];
			Iterator<String> sqlValueIter = sqlValue.iterator();
			while ( sqlValueIter.hasNext() )
			{
				oValue[k] = columnValue.get(sqlValueIter.next());
				k++;
			}			
			arrayData.add(oValue);
		}

		// sql
		StringBuilder sbKey = new StringBuilder();
		StringBuilder sbValue = new StringBuilder();
		Object[] sqlArray = sqlValue.toArray();
		for ( int i = 0; i < sqlArray.length; i++ )
		{
			sbKey.append(sqlArray[i].toString() + ",");
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

	public static JsonArray readJsonFromUrl(String url) throws IOException
	{
		InputStream is = new URL(url).openStream();
		try
		{
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JsonArray jsonObject = ( new JsonParser() ).parse(jsonText).getAsJsonArray();
			return jsonObject;
		}
		finally
		{
			is.close();
		}
	}

	private static String readAll(Reader rd) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		int cp;
		while ( ( cp = rd.read() ) != -1 )
		{
			sb.append((char)cp);
		}
		return sb.toString();
	}
}
