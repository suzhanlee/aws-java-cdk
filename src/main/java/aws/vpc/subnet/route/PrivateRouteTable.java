package aws.vpc.subnet.route;

import aws.vpc.subnet.dto.NatGatewayDto;
import aws.vpc.subnet.dto.SubnetDto;
import java.util.Optional;
import software.amazon.awscdk.services.ec2.CfnRoute;
import software.amazon.awscdk.services.ec2.CfnRouteTable;
import software.amazon.awscdk.services.ec2.CfnSubnetRouteTableAssociation;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

public class PrivateRouteTable implements RouteTable {
    private static final String CIDR = "0.0.0.0/0";

    private final Construct scope;
    private final Vpc vpc;
    private final String routeTableId;
    private final String privateRouteId;
    private final Optional<NatGatewayDto> natGateway;

    public PrivateRouteTable(Construct scope, Vpc vpc, String routeTableId, String privateRouteId,
                             Optional<NatGatewayDto> natGateway) {
        this.scope = scope;
        this.vpc = vpc;
        this.routeTableId = routeTableId;
        this.privateRouteId = privateRouteId;
        this.natGateway = natGateway;
    }

    @Override
    public void configure(SubnetDto subnetDto) {
        createRouteTable();
        associateRouteTableWithSubnet(subnetDto);
        routePrivateSubnetToNatGateway();
    }

    private CfnRouteTable createRouteTable() {
        return CfnRouteTable.Builder.create(scope, routeTableId)
                .vpcId(vpc.getVpcId())
                .build();
    }

    private void routePrivateSubnetToNatGateway() {
        natGateway.ifPresentOrElse(
                ngw -> assignRoute(privateRouteId, ngw),
                () -> assignRoute(privateRouteId)
        );
    }

    private void assignRoute(String routeId, NatGatewayDto natGateway) {
        CfnRoute.Builder.create(scope, routeId)
                .routeTableId(routeTableId)
                .destinationCidrBlock(CIDR)
                .natGatewayId(natGateway.id())
                .build();
    }

    private void assignRoute(String routeId) {
        CfnRoute.Builder.create(scope, routeId)
                .routeTableId(routeTableId)
                .destinationCidrBlock(CIDR)
                .build();
    }

    private CfnSubnetRouteTableAssociation associateRouteTableWithSubnet(SubnetDto subnetDto) {
        if (subnetDto.az().existsFirstAZ()) {
            return createAssociation(subnetDto.id(), "PrivateSubnet1RouteTableAssociation");
        }
        return createAssociation(subnetDto.id(), "PrivateSubnet2RouteTableAssociation");
    }

    private CfnSubnetRouteTableAssociation createAssociation(String subnetId, String associationId) {
        return CfnSubnetRouteTableAssociation.Builder.create(scope, associationId)
                .subnetId(subnetId)
                .routeTableId(routeTableId)
                .build();
    }
}
