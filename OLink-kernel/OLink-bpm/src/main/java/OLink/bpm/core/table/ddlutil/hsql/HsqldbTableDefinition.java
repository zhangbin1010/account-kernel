package OLink.bpm.core.table.ddlutil.hsql;

import java.sql.Connection;
import java.sql.SQLException;

import OLink.bpm.core.table.model.Table;
import OLink.bpm.core.dynaform.document.dql.DQLASTUtil;
import OLink.bpm.core.table.alteration.ColumnDataTypeChange;
import OLink.bpm.core.table.ddlutil.AbstractTableDefinition;
import OLink.bpm.util.DbTypeUtil;
import org.apache.log4j.Logger;

import OLink.bpm.core.table.alteration.ColumnRenameChange;
import OLink.bpm.core.table.model.Column;

/**
 * 
 * @author Chris
 * 
 */
public class HsqldbTableDefinition extends AbstractTableDefinition {
	protected static Logger log = Logger.getLogger(HsqldbTableDefinition.class);

	public HsqldbTableDefinition(Connection conn) {
		super(conn, new HsqldbBuilder());
		this.schema = DbTypeUtil.getSchema(conn, DbTypeUtil.DBTYPE_HSQLDB);
		_builder.setSchema(schema);
	}

	public void processChange(ColumnDataTypeChange change) throws SQLException {
		StringBuffer buffer = new StringBuffer();
		getSQLBuilder().setWriter(buffer);

		Table table = change.getTable();
		Column targetColumn = change.getTargetColumn();
		Column sourceColumn = change.getSourceColumn();
		Column tempColumn = sourceColumn;
		// boolean isDataExists = false;

		try {
			tempColumn = (Column) sourceColumn.clone();
			log.debug(Integer.valueOf(sourceColumn.hashCode()));
			log.debug(Integer.valueOf(tempColumn.hashCode()));
			tempColumn.setName(DQLASTUtil.TEMP_PREFIX + sourceColumn.getName());
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		// modify column type need 4 step:
		// step1-->rename source column to temp column
		getSQLBuilder().columnRename(table, sourceColumn, tempColumn);

		// step2-->add target column
		getSQLBuilder().addColumn(table, targetColumn);

		// step3-->transfer data,from souce column to target column
		getSQLBuilder().columnDataCopy(table, tempColumn, targetColumn);

		// step4-->drop temp cloumn
		getSQLBuilder().dropColumn(table, tempColumn);

		String sql = getSQLBuilder().getSQL();
		evaluateBatch(sql, false);
	}

	public void processChange(ColumnRenameChange change) throws SQLException {
		StringBuffer buffer = new StringBuffer();
		getSQLBuilder().setWriter(buffer);
		getSQLBuilder().columnRename(change.getTable(), change.getSourceColumn(), change.getTargetColumn());

		String sql = getSQLBuilder().getSQL();
		evaluateBatch(sql, false);
	}

}
