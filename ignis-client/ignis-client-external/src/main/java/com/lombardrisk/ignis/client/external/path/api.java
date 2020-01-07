package com.lombardrisk.ignis.client.external.path;

@SuppressWarnings("all")
public interface api {

    interface external {

        interface v1 {

            String Version1 = "api/v1";
            String Jobs = Version1 + "/jobs";
            String Staging = "api/v1/stagingItems";
            String Datasets = Version1 + "/datasets";

            interface jobs {

                String ById = Jobs + "/{" + Params.ID + "}";

                interface byID {

                    String Stop = ById + "/stop";
                }
            }

            interface staging {

                String ById = Staging + "/{" + Params.ID + "}";

                interface byID {

                    String ValidationErrorFile = ById + "/validationError";
                }
            }

            interface datasets {

                String ByID = Datasets + "/{" + Params.ID + "}";

                interface byID {

                    String ValidationResultsSummaries = ByID + "/validationResultsSummaries";
                    String ValidationResultsDetails = ByID + "/validationResultsDetails";
                }
            }

            String Pipelines = Version1 + "/pipelines";
            String PipelineInvocations = Version1 + "/pipelineInvocations";
            String ProductConfigs = Version1 + "/productConfigs";
            String Calendars = Version1 + "/calendars";
            String PipelinesDownstreams = Version1 + "/pipelinesDownstreams";
            String WorkingDays = Version1 + "/workingDays";

            interface productConfigs {

                String ById = ProductConfigs + "/{" + Params.ID + "}";

                interface byID {

                    String File = ById + "/file";
                    String WorkingDays = ById + "/workingDays";
                }

                String File = ProductConfigs + "/file";
            }

            interface drillBack {

                String DrillBack = Version1 + "/drillback";
                String Metadata = DrillBack + "/metadata";

                interface dataset {

                    String byId = DrillBack + "/datasets/{" + Params.DATASET_ID + "}";
                    String export = byId + "/export";
                    String exportOnlyDrillback = byId + "/exportOnlyDrillback";
                }

                interface pipeline {

                    String Pipeline = DrillBack + "/pipelines/" + "/{" + Params.ID + "}";

                    interface step {

                        String byId = Pipeline + "/steps/{" + Params.STEP_ID + "}";
                    }
                }
            }

            interface pipelines {

                String byId = Pipelines + "/{" + Params.ID + "}";

                interface ByID {

                    String schemas = byId + "/schemas";
                    String edges = byId + "/edges";
                }
            }

            interface pipelineInvocations {

                String byJobId = PipelineInvocations + "/{" + Params.JOB_ID + "}";
            }

            public interface calendars {

                String byId = Calendars + "/{" + Params.ID + "}";
            }
        }

        interface v2 {

            String Version2 = "api/v2";
            String Jobs = Version2 + "/jobs";
        }
    }

    interface Params {

        String DATASET_NAME = "datasetName";
        String DATASET_SCHEMA = "datasetSchema";
        String ITEM_NAME = "itemName";
        String ENTITY_CODE = "entityCode";
        String REFERENCE_DATE = "referenceDate";
        String RUN_KEY = "runKey";
        String ID = "id";
        String PRODUCT_ID = "id";
        String PRODUCT_NAME = "productName";
        String DATASET_ID = "datasetId";
        String SELECTED_ROW_KEY = "selectedRowKey";
        String PIPELINE_ID = "pipelineId";
        String PIPELINE_INVOCATION_ID = "pipelineInvocationId";
        String PIPELINE_STEP_ID = "pipelineStepId";
        String PIPELINE_STEP_INVOCATION_ID = "pipelineStepInvocationId";
        String STEP_ID = "stepId";
        String SCHEMA_ID = "schemaId";
        String JOB_ID = "jobId";
        String RULE_ID = "ruleId";
        String FILE = "file";
        String STATE = "state";
        String OUTPUT_TABLE_ROWKEY = "outputTableRowKey";
    }
}
