package com.lombardrisk.ignis.spark.script.demo;

import com.lombardrisk.ignis.spark.script.api.Scriptlet;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("squid:S1192")
public class TotalTradesByTrader implements Scriptlet {

    private static final StructType TRADERS_SCHEMA = new StructType(new StructField[]{
            new StructField("TRADER_ID", DataTypes.IntegerType, false, Metadata.empty()),
            new StructField("Name", DataTypes.StringType, false, Metadata.empty()),
            new StructField("TRADE_DATE", DataTypes.IntegerType, false, Metadata.empty()),
    });

    private static final StructType TRADES_SCHEMA = new StructType(new StructField[]{
            new StructField("TRADE_DATE", DataTypes.IntegerType, false, Metadata.empty()),
            new StructField("STOCK", DataTypes.StringType, false, Metadata.empty()),
            new StructField("TradeType", DataTypes.StringType, false, Metadata.empty()),
    });

    private static final StructType TRADES_BY_TRADER_SCHEMA = new StructType(new StructField[]{
            new StructField("TRADER_ID", DataTypes.IntegerType, false, Metadata.empty()),
            new StructField("Name", DataTypes.StringType, false, Metadata.empty()),
            new StructField("STOCK", DataTypes.StringType, false, Metadata.empty()),
            new StructField("TradeType", DataTypes.StringType, false, Metadata.empty()),
            new StructField("Total", DataTypes.LongType, false, Metadata.empty()),
    });

    @Override
    public Dataset<Row> run(final Map<String, Dataset<Row>> inputs) {
        Dataset<Row> traders = inputs.get("Traders");
        Dataset<Row> trades = inputs.get("Trades");

        Dataset<Row> output = traders
                .join(trades, traders.col("TRADE_DATE").equalTo(trades.col("TRADE_DATE")))
                .groupBy(
                        traders.col("TRADER_ID"),
                        trades.col("STOCK"),
                        trades.col("TradeType"))
                .agg(functions.first("Name").as("Name"), functions.count("*").as("Total"));

        output.printSchema();
        output.show();

        return output;
    }

    @Override
    public Map<String, StructType> inputTraits() {
        Map<String, StructType> inputs = new HashMap<>();
        inputs.put("Traders", TRADERS_SCHEMA);
        inputs.put("Trades", TRADES_SCHEMA);
        return inputs;
    }

    @Override
    public StructType outputTrait() {
        return TRADES_BY_TRADER_SCHEMA;
    }
}
