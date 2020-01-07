package com.lombardrisk.ignis.client.design.path;

import static com.lombardrisk.ignis.client.design.path.design.api.Params.FIELD_ID;
import static com.lombardrisk.ignis.client.design.path.design.api.Params.JAR_FILE_NAME;
import static com.lombardrisk.ignis.client.design.path.design.api.Params.PIPELINE_ID;
import static com.lombardrisk.ignis.client.design.path.design.api.Params.PRODUCT_ID;
import static com.lombardrisk.ignis.client.design.path.design.api.Params.REQUIRED_PIPELINE_IDS;
import static com.lombardrisk.ignis.client.design.path.design.api.Params.ROW_CELL_DATA_ID;
import static com.lombardrisk.ignis.client.design.path.design.api.Params.ROW_ID;
import static com.lombardrisk.ignis.client.design.path.design.api.Params.RULE_ID;
import static com.lombardrisk.ignis.client.design.path.design.api.Params.SCHEMA_ID;
import static com.lombardrisk.ignis.client.design.path.design.api.Params.SCRIPTLET_CLASS_NAME;
import static com.lombardrisk.ignis.client.design.path.design.api.Params.STEP_ID;
import static com.lombardrisk.ignis.client.design.path.design.api.Params.TEST_ID;

@SuppressWarnings("all")
public interface design {

    interface api {

        interface v1 {

            String Version1 = "api/v1";

            String ProductConfigs = Version1 + "/productConfigs";

            interface productConfigs {

                String ById = ProductConfigs + "/{" + PRODUCT_ID + "}";
                String File = ProductConfigs + "/file";

                interface productId {

                    String File = ById + "/file";
                    String FileByRequiredPipelines = ById + "/file" + "/{" + REQUIRED_PIPELINE_IDS + "}";
                    String Schemas = ById + "/schemas";
                    String Tasks = ById + "/tasks";
                    String Validate = ById + "/validate";

                    interface schemas {

                        String ById = Schemas + "/{" + SCHEMA_ID + "}";
                        String Copy = Schemas + "/{" + SCHEMA_ID + "}/copy";

                        interface schemaId {

                            String Rules = ById + "/rules";

                            interface rules {

                                String ById = Rules + "/{" + RULE_ID + "}";

                                interface ruleId {

                                    String Examples = ById + "/examples";
                                }
                            }

                            String Fields = ById + "/fields";

                            interface fields {

                                String ById = Fields + "/{" + FIELD_ID + "}";
                            }
                        }
                    }
                }
            }

            String Pipelines = Version1 + "/pipelines";

            interface pipelines {

                String ById = Pipelines + "/{" + PIPELINE_ID + "}";
                String SyntaxCheck = Pipelines + "/syntax";

                interface pipelineId {

                    String Edges = ById + "/edges";
                    String Steps = ById + "/steps";

                    interface steps {

                        String ById = Steps + "/{" + STEP_ID + "}";

                    }
                }
            }

            interface feedback {

                String Submit = Version1 + "/feedback";
            }

            String PipelineStepTests = Version1 + "/pipelineStepTests";
            interface pipelineStepTests {


                String ById = PipelineStepTests + "/{" + TEST_ID + "}";
                String run = ById + "/run";

                String inputDataRow = ById + "/inputDataRows";
                String importCsvInputDataRow = ById + "/{" + SCHEMA_ID + "}" + "/importCsvInputDataRow";
                String importCsvExpectedDataRow = ById + "/{" + SCHEMA_ID + "}" + "/importCsvExpectedDataRow";
                String expectedDataRow = ById + "/expectedDataRows";
                String outputDataRow = ById + "/outputDataRows";

                String importCsv = ById + "/schemas/{" + SCHEMA_ID + "}" + "/import";
                String exportCsv = ById + "/schemas/{" + SCHEMA_ID + "}" + "/export";

                String rowCellDataByRowCellDataId = ById + "/rows/{" + ROW_ID + "}" + "/rowCells" + "/{" + ROW_CELL_DATA_ID + "}";
                String inputDataRowById = inputDataRow + "/{" + ROW_ID + "}";
                String expectedDataRowById = expectedDataRow + "/{" + ROW_ID + "}";
            }

            String Scriptlets = Version1 + "/scriptlets";

            interface scriptlets {
                String Jars = Scriptlets + "/jars";
                String ScriptletClasses = Scriptlets + "/jars/{" + JAR_FILE_NAME + "}" + "/classes";
                String ScriptletClassMetadata =
                        Scriptlets + "/jars/{" + JAR_FILE_NAME + "}/classes/{" + SCRIPTLET_CLASS_NAME + "}/metadata";
            }
        }

        interface Params {
            String PRODUCT_ID = "productId";
            String SCHEMA_ID = "schemaId";
            String RULE_ID = "ruleId";
            String FIELD_ID = "fieldId";
            String FILE = "file";
            String REQUIRED_PIPELINE_IDS = "requiredPipelineIds";
            String PIPELINE_ID = "pipelineId";
            String STEP_ID = "stepId";
            String TEST_ID = "testId";
            String ROW_ID = "rowId";
            String ROW_CELL_DATA_ID = "rowCellDataId";
            String JAR_FILE_NAME = "jarFileName";
            String SCRIPTLET_CLASS_NAME = "classname";
        }
    }
}
