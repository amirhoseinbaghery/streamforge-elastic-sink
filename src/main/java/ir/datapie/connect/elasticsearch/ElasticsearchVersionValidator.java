package ir.datapie.connect.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

public final class ElasticsearchVersionValidator {
    public String validate(ElasticsearchClient client, int expectedMajor) throws Exception {
        String version = client.info().version().number();
        String major = version.split("\\.", 2)[0];
        if (Integer.parseInt(major) != expectedMajor) {
            throw new IllegalStateException("Unexpected Elasticsearch major=" + major + ", expected=" + expectedMajor);
        }
        return version;
    }
}
