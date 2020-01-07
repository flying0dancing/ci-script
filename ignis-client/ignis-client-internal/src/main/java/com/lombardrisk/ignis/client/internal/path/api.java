package com.lombardrisk.ignis.client.internal.path;

import static com.lombardrisk.ignis.client.internal.path.api.Params.ID;
import static com.lombardrisk.ignis.client.internal.path.api.Params.NAME;
import static com.lombardrisk.ignis.client.internal.path.api.Params.USERNAME;

@SuppressWarnings("all")
public interface api {

    String Internal = "/api/internal";

    interface internal {

        String Features = "api/internal/features";
        String Datasets = "api/internal/datasets";
        String PipelineInvocations = "api/internal/pipelineInvocations";

        interface datasets {

            String byId = "api/internal/datasets/{" + ID + "}";

            interface byID {

                String ValidationResultsSummaries = byId + "/validationResultsSummaries";
            }

            interface byNAME {

                String LastVersion = "api/internal/datasets/{" + NAME + "}/lastVersion";
            }

            interface source {

                String Files = "api/internal/datasets/source/files";
            }
        }

        interface features {

            String byName = Features + "/{" + NAME + "}";
        }

        interface ruleSets {

            interface byID {

                String Rule = "api/internal/ruleSets/{" + ID + "}/rule";
            }
        }

        String Tables = "api/internal/tables";

        interface tables {

            String ById = "api/internal/tables/{" + ID + "}";

            interface byID {

                String Rule = ById + "/rule";
            }

            interface search {

                String FindByName = "api/internal/tables/search/findByName";
            }

            String File = "api/internal/tables/file";
        }

        String ProductConfigs = "api/internal/productConfigs";

        interface productConfigs {

            String ById = "api/internal/productConfigs/{" + ID + "}";
        }

        String Users = "api/internal/users";

        interface users {

            String ByUsername = "api/internal/users/{" + USERNAME + "}";
            String CurrentUser = "api/internal/users/currentUser";
        }

        interface pipelineInvocations {

            String ById = PipelineInvocations + "/{" + ID + "}";
        }
    }

    interface Params {

        String ID = "id";
        String NAME = "name";
        String DATASET = "dataset";
        String ENTITY_CODE_FIELD = "entityCodeField";
        String ENTITY_CODE = "entityCode";
        String FILE = "file";
        String METADATA = "metadata";
        String REFERENCE_DATE_FIELD = "referenceDateField";
        String REFERENCE_DATE = "referenceDate";
        String TABLE_NAME = "tableName";
        String USERNAME = "username";
    }
}












