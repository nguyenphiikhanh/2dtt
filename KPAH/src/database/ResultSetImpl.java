/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Dev Duy
 */
public class ResultSetImpl {

    private Map<String, Object>[] data;
    private Object[][] values;
    private int indexData;

    public ResultSetImpl(final ResultSet rs) throws SQLException {
        this.indexData = -1;
        try {
            rs.last();
            final int nRow = rs.getRow();
            rs.beforeFirst();
            final ResultSetMetaData rsmd = rs.getMetaData();
            final int nColumn = rsmd.getColumnCount();
            this.data = new HashMap[nRow];
            for (int i = 0; i < this.data.length; ++i) {
                this.data[i] = new HashMap<>();
            }
            this.values = new Object[nRow][nColumn];
            int index = 0;
            while (rs.next()) {
                for (int j = 1; j <= nColumn; ++j) {
                    final String tableName = rsmd.getTableName(j);
                    final String columnName = rsmd.getColumnName(j);
                    final Object columnValue = rs.getObject(j);
                    this.data[index].put(columnName.toLowerCase(), columnValue);
                    this.data[index].put(tableName.toLowerCase() + "." + columnName.toLowerCase(), columnValue);
                    this.values[index][j - 1] = columnValue;
                }
                ++index;
            }
        } catch (final SQLException e) {
            throw e;
        } finally {
            if (rs != null) {
                rs.getStatement().close();
                rs.close();
            }
        }
    }

    public void close() {
        for (Map map : this.data) {
            if (map != null) {
                map.clear();
                map = null;
            }
        }
        this.data = null;
        for (final Object[] array : this.values) {
            for (Object o : array) {
                o = null;
            }
        }
        this.values = null;
    }

    public boolean next() throws SQLException {
        if (this.data == null) {
            throw new SQLException("No data available");
        }
        ++this.indexData;
        return this.indexData < this.data.length;
    }

    public boolean first() throws SQLException {
        if (this.data == null) {
            throw new SQLException("No data available");
        }
        ++this.indexData;
        return this.indexData == this.data.length - 1;
    }

    public boolean gotoResult(final int index) throws SQLException {
        if (this.data == null) {
            throw new SQLException("No data available");
        }
        if (this.indexData < 0 || this.indexData >= this.data.length) {
            throw new SQLException("Index out of bound");
        }
        this.indexData = index;
        return true;
    }

    public boolean gotoFirst() throws SQLException {
        if (this.data == null || this.data.length == 0) {
            throw new SQLException("No data available");
        }
        this.indexData = 0;
        return true;
    }

    public void gotoBeforeFirst() {
        this.indexData = -1;
    }

    public boolean gotoLast() throws SQLException {
        if (this.data == null) {
            throw new SQLException("No data available");
        }
        this.indexData = this.data.length - 1;
        return true;
    }

    public int getRows() throws SQLException {
        if (this.data == null) {
            throw new SQLException("No data available");
        }
        return this.data.length;
    }

    public byte getByte(final int column) throws SQLException {
        if (this.values == null) {
            throw new SQLException("No data available");
        }
        if (this.indexData == -1) {
            throw new SQLException("Results need to be prepared in advance");
        }
        return (byte) (int) this.values[this.indexData][column - 1];
    }

    public byte getByte(final String column) throws SQLException {
        if (this.data == null) {
            throw new SQLException("No data available");
        }
        if (this.indexData == -1) {
            throw new SQLException("Results need to be prepared in advance");
        }
        return (byte) (int) this.data[this.indexData].get(column.toLowerCase());
    }

    public int getInt(final int column) throws SQLException {
        if (this.values == null) {
            throw new SQLException("No data available");
        }
        if (this.indexData == -1) {
            throw new SQLException("Results need to be prepared in advance");
        }
        return (int) this.values[this.indexData][column - 1];
    }

    public int getInt(final String column) throws SQLException {
        if (this.data == null) {
            throw new SQLException("No data available");
        }
        if (this.indexData == -1) {
            throw new SQLException("Results need to be prepared in advance");
        }
        return (int) this.data[this.indexData].get(column.toLowerCase());
    }

    public float getFloat(final int column) throws SQLException {
        if (this.values == null) {
            throw new SQLException("No data available");
        }
        if (this.indexData == -1) {
            throw new SQLException("Results need to be prepared in advance");
        }
        return (float) this.values[this.indexData][column - 1];
    }

    public float getFloat(final String column) throws SQLException {
        if (this.data == null) {
            throw new SQLException("No data available");
        }
        if (this.indexData == -1) {
            throw new SQLException("Results need to be prepared in advance");
        }
        return (float) this.data[this.indexData].get(column.toLowerCase());
    }

    public double getDouble(final int column) throws SQLException {
        if (this.values == null) {
            throw new SQLException("No data available");
        }
        if (this.indexData == -1) {
            throw new SQLException("Results need to be prepared in advance");
        }
        return (double) this.values[this.indexData][column - 1];
    }

    public double getDouble(final String column) throws SQLException {
        if (this.data == null) {
            throw new SQLException("No data available");
        }
        if (this.indexData == -1) {
            throw new SQLException("Results need to be prepared in advance");
        }
        return (double) this.data[this.indexData].get(column.toLowerCase());
    }

    public long getLong(final int column) throws SQLException {
        if (this.values == null) {
            throw new SQLException("No data available");
        }
        if (this.indexData == -1) {
            throw new SQLException("Results need to be prepared in advance");
        }
        return (long) this.values[this.indexData][column - 1];
    }

    public long getLong(final String column) throws SQLException {
        if (this.data == null) {
            throw new SQLException("No data available");
        }
        if (this.indexData == -1) {
            throw new SQLException("Results need to be prepared in advance");
        }
        return Long.parseLong(this.data[this.indexData].get(column.toLowerCase()).toString());
    }

    public String getString(final int column) throws SQLException {
        if (this.values == null) {
            throw new SQLException("No data available");
        }
        if (this.indexData == -1) {
            throw new SQLException("Results need to be prepared in advance");
        }
        return String.valueOf(this.values[this.indexData][column - 1]);
    }

    public String getString(final String column) throws SQLException {
        if (this.data == null) {
            throw new SQLException("No data available");
        }
        if (this.indexData == -1) {
            throw new SQLException("Results need to be prepared in advance");
        }
        return String.valueOf(this.data[this.indexData].get(column.toLowerCase()));
    }

    public Object getObject(final int column) throws SQLException {
        if (this.values == null) {
            throw new SQLException("No data available");
        }
        if (this.indexData == -1) {
            throw new SQLException("Results need to be prepared in advance");
        }
        return this.values[this.indexData][column - 1];
    }

    public Object getObject(final String column) throws SQLException {
        if (this.data == null) {
            throw new SQLException("No data available");
        }
        if (this.indexData == -1) {
            throw new SQLException("Results need to be prepared in advance");
        }
        return this.data[this.indexData].get(column.toLowerCase());
    }

    public boolean getBoolean(final int column) throws SQLException {
        if (this.values == null) {
            throw new SQLException("No data available");
        }
        if (this.indexData == -1) {
            throw new SQLException("Results need to be prepared in advance");
        }
        try {
            return (int) this.values[this.indexData][column - 1] == 1;
        } catch (final Exception e) {
            return (boolean) this.values[this.indexData][column - 1];
        }
    }

    public boolean getBoolean(final String column) throws SQLException {
        if (this.data == null) {
            throw new SQLException("No data available");
        }
        if (this.indexData == -1) {
            throw new SQLException("Results need to be prepared in advance");
        }
        try {
            return (int) this.data[this.indexData].get(column.toLowerCase()) == 1;
        } catch (final Exception e) {
            return (boolean) this.data[this.indexData].get(column.toLowerCase());
        }
    }

    public Timestamp getTimestamp(final int column) throws SQLException {
        if (this.values == null) {
            throw new SQLException("No data available");
        }
        if (this.indexData == -1) {
            throw new SQLException("Results need to be prepared in advance");
        }
        return (Timestamp) this.values[this.indexData][column - 1];
    }

    public Timestamp getTimestamp(final String column) throws SQLException {
        if (this.data == null) {
            throw new SQLException("No data available");
        }
        if (this.indexData == -1) {
            throw new SQLException("Results need to be prepared in advance");
        }
        return (Timestamp) this.data[this.indexData].get(column.toLowerCase());
    }

    public short getShort(final int column) throws SQLException {
        if (this.values == null) {
            throw new SQLException("No data available");
        }
        if (this.indexData == -1) {
            throw new SQLException("Results need to be prepared in advance");
        }
        return (short) (int) this.values[this.indexData][column - 1];
    }

    public short getShort(final String column) throws SQLException {
        if (this.data == null) {
            throw new SQLException("No data available");
        }
        if (this.indexData == -1) {
            throw new SQLException("Results need to be prepared in advance");
        }
        return (short) (int) this.data[this.indexData].get(column.toLowerCase());
    }
}
