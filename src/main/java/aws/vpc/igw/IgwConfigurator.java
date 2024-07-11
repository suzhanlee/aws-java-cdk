package aws.vpc.igw;

import software.amazon.awscdk.services.ec2.CfnInternetGateway;
import software.amazon.awscdk.services.ec2.CfnVPCGatewayAttachment;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

public class IgwConfigurator {
    private static final String ATTACHMENT_SUFFIX = "attachment";

    private final Construct scope;
    private final Vpc vpc;

    public IgwConfigurator(Construct scope, Vpc vpc) {
        this.scope = scope;
        this.vpc = vpc;
    }

    public String configure(String igwId) {
        CfnInternetGateway igw = createIgw(igwId);
        attachIgwToVpc(igwId, igw);
        return igw.getAttrInternetGatewayId();
    }

    private CfnInternetGateway createIgw(String igwId) {
        return CfnInternetGateway.Builder.create(scope, igwId).build();
    }

    private void attachIgwToVpc(String igwId, CfnInternetGateway igw) {
        CfnVPCGatewayAttachment.Builder.create(scope, igwId + ATTACHMENT_SUFFIX)
                .vpcId(vpc.getVpcId())
                .internetGatewayId(igw.getAttrInternetGatewayId())
                .build();
    }
}
